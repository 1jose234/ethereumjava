/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.samples;

import org.ethereum.core.Block;
import org.ethereum.core.CallTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.solidity.compiler.CompilationResult;
import org.ethereum.solidity.compiler.SolidityCompiler;
import org.ethereum.util.ByteUtil;
import org.ethereum.vm.program.ProgramResult;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Anton Nashatyrev on 03.03.2016.
 */
public class CreateContractSample extends ConfConexionPrivada {

    @Autowired
    SolidityCompiler compiler;

    String contract =
            "contract Sample {" +
            "  int i;" +
            "  function inc(int n) {" +
            "    i = i + n;" +
            "  }" +
            "  function get() returns (int) {" +
            "    return i;" +
            "  }" +
            "}";

    private Map<ByteArrayWrapper, TransactionReceipt> txWaiters =
            Collections.synchronizedMap(new HashMap<ByteArrayWrapper, TransactionReceipt>());

    @Override
    public void onSyncDone() throws Exception {
        ethereum.addListener(new EthereumListenerAdapter() {
            // when block arrives look for our included transactions
            @Override
            public void onBlock(Block block, List<TransactionReceipt> receipts) {
                CreateContractSample.this.onBlock(block, receipts);
            }
        });

        logger.info("Compiling contract...");
        SolidityCompiler.Result result = compiler.compileSrc(contract.getBytes(), true, true,
                SolidityCompiler.Options.ABI, SolidityCompiler.Options.BIN);
        if (result.isFailed()) {
            throw new RuntimeException("Contract compilation failed:\n" + result.errors);
        }
        CompilationResult res = CompilationResult.parse(result.output);
        if (res.getContracts().isEmpty()) {
            throw new RuntimeException("Compilation failed, no contracts returned:\n" + result.errors);
        }
        CompilationResult.ContractMetadata metadata = res.getContracts().iterator().next();
        if (metadata.bin == null || metadata.bin.isEmpty()) {
            throw new RuntimeException("Compilation failed, no binary returned:\n" + result.errors);
        }

        logger.info("Sending contract to net and waiting for inclusion");
        //TODO Modify, its only for test
        ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");
        TransactionReceipt receipt = sendTxAndWait(new byte[0],  Hex.decode(metadata.bin));

        if (!receipt.isSuccessful()) {
            logger.error("Some troubles creating a contract: " + receipt.getError());
            return;
        }

        byte[] contractAddress = receipt.getTransaction().getContractAddress();
        logger.info("Contract created: " + Hex.toHexString(contractAddress));

        logger.info("Calling the contract function 'inc'");
        CallTransaction.Contract contract = new CallTransaction.Contract(metadata.abi);
        CallTransaction.Function inc = contract.getByName("inc");
        byte[] functionCallBytes = inc.encode(777);
        
        ProgramResult r = ethereum.callConstantFunction(Hex.toHexString(contractAddress),contract.getByName("get"));
      Object[] ret = contract.getByName("get").decodeResult(r.getHReturn());
      logger.info("Current contract data member value: " + ret[0]);
        
        
        TransactionReceipt receipt1 = sendTxAndWait(contractAddress, functionCallBytes);
        
        if (!receipt1.isSuccessful()) {
            logger.error("Some troubles invoking the contract: " + receipt.getError());
            return;
        }

        byte[] contractAddresss = receipt.getTransaction().getContractAddress();
        
        ProgramResult rs = ethereum.callConstantFunction(Hex.toHexString(contractAddresss),contract.getByName("get"));
        Object[] rets = contract.getByName("get").decodeResult(rs.getHReturn());
        logger.info("Current contract data member value: " + rets[0]);
        
        logger.info("Contract modified!");

//        ProgramResult r = ethereum.callConstantFunction(Hex.toHexString(contractAddress),contract.getByName("get"));
//        Object[] ret = contract.getByName("get").decodeResult(r.getHReturn());
//        logger.info("Current contract data member value: " + ret[0]);
        logger.info("      ************ MODIFICACION ********** \n     "
        		+ "********************************************** \n" );
    }

    protected TransactionReceipt sendTxAndWait(byte[] receiveAddress, byte[] data) throws InterruptedException {
        //TODO Modify, its only for test
    	ECKey senderKey = ECKey.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
        BigInteger nonce = ethereum.getRepository().getNonce(senderKey.getAddress());
        Transaction tx = new Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.longToBytesNoLeadZeroes(ethereum.getGasPrice()),
                ByteUtil.longToBytesNoLeadZeroes(3_000_000),
                receiveAddress,
                ByteUtil.longToBytesNoLeadZeroes(0),
                data,
                ethereum.getChainIdForNextBlock());
        //tx.sign(ECKey.fromPrivate(senderPrivateKey));
       
        tx.sign(senderKey);
        
        logger.info("<=== Sending transaction: " + tx);
        ethereum.submitTransaction(tx);

        return waitForTx(tx.getHash());
    }
    
    

    private void onBlock(Block block, List<TransactionReceipt> receipts) {
        for (TransactionReceipt receipt : receipts) {
            ByteArrayWrapper txHashW = new ByteArrayWrapper(receipt.getTransaction().getHash());
            if (txWaiters.containsKey(txHashW)) {
                txWaiters.put(txHashW, receipt);
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    protected TransactionReceipt waitForTx(byte[] txHash) throws InterruptedException {
        ByteArrayWrapper txHashW = new ByteArrayWrapper(txHash);
        txWaiters.put(txHashW, null); //HERE SET NULL ALL TIME
        long startBlock = ethereum.getBlockchain().getBestBlock().getNumber();
        while(true) {
            TransactionReceipt receipt = txWaiters.get(txHashW);
            if (receipt != null) {
                return receipt;
            } else {
                long curBlock = ethereum.getBlockchain().getBestBlock().getNumber();
                if (curBlock > startBlock + 16) {
                    throw new RuntimeException("The transaction was not included during last 16 blocks: " + txHashW.toString().substring(0,8));
                } else {
                    logger.info("Waiting for block with transaction 0x" + txHashW.toString().substring(0,8) +
                            " included (" + (curBlock - startBlock) + " blocks received so far) ...");
                }
            }
            synchronized (this) {
                wait(20000);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        class Config extends TestNetConfig{
            @Override
            @Bean
            public ConfConexionPrivada sampleBean() {
                return new CreateContractSample();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(Config.class);
    }
}
