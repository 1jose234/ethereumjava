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

import com.typesafe.config.ConfigFactory;
import org.ethereum.config.SystemProperties;
import org.ethereum.crypto.ECKey;
import org.ethereum.facade.EthereumFactory;
import org.springframework.context.annotation.Bean;

import static org.ethereum.crypto.HashUtil.sha3;

/**
 * This class just extends the BasicSample with the config which connect the peer to the test network
 * This class can be used as a base for free transactions testing
 * (everyone may use that 'cow' sender which has pretty enough fake coins)
 *
 * Created by Anton Nashatyrev on 10.02.2016.
 */
public class ConfConexionPrivada extends BasicSample {
    /**
     * Use that sender key to sign transactions
     */
    protected final byte[] senderPrivateKey = sha3("cow".getBytes());
    // sender address is derived from the private key
    protected final byte[] senderAddress = ECKey.fromPrivate(senderPrivateKey).getAddress();

    protected abstract static class TestNetConfig {
//        private final String config =
//                // Ropsten revive network configuration
//                "peer.discovery.enabled = true \n" +
//                "peer.listen.port = 30303 \n" +
//                "peer.networkId = 3 \n" +
//                // a number of public peers for this network (not all of then may be functioning)
//                "peer.active = [" +
//                "    {url = 'enode://6ce05930c72abc632c58e2e4324f7c7ea478cec0ed4fa2528982cf34483094e9cbc9216e7aa349691242576d552a2a56aaeae426c5303ded677ce455ba1acd9d@13.84.180.240:30303'}," +
//                "    {url = 'enode://20c9ad97c081d63397d7b685a412227a40e23c8bdc6688c6f37e97cfbc22d2b4d1db1510d8f61e6a8866ad7f0e17c02b14182d37ea7c3c8b9c2683aeb6b733a1@52.169.14.227:30303'}" +
//                "] \n" +
//                "sync.enabled = true \n" +
//                // special genesis for this test network
//                "genesis = ropsten.json \n" +
//                "blockchain.config.name = 'ropsten' \n" +
//                "database.dir = testnetSampleDb \n" +
//                "cache.flush.memory = 0";
        
        private final String config =
                // no discovery: we are connecting directly to the miner peer
                "peer.discovery.enabled = false \n" +
                "peer.listen.port = 30341 \n" +
//                "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
                "peer.networkId = 556 \n" +
                // actively connecting to the miner
                "peer.active = [" +
                "    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30340' }" +
                "] \n" +
                "sync.enabled = true \n" +
                // all peers in the same network need to use the same genesis block
                "genesis = sample-genesis.json \n" +
//                "blockchain.config.name = 'testnet' \n" +
                // two peers need to have separate database dirs
                "database.dir = confConecionPrivada \n" +
        		"cache.flush.memory = 0";
        
//        private final String config =
//                // no discovery: we are connecting directly to the miner peer
//                "peer.discovery.enabled = false \n" +
//                "peer.listen.port = 30341 \n" +
////                "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
//                "peer.networkId = 1 \n" +
//                // actively connecting to the miner
//                "peer.active = [" +
//                "    { url = 'enode://00544537575ac3a4470f81d7a002e9fe9e4f41364f81bc75d4ab2dfcb5f1aa26cbab5f1fd4db705396eaa1391e86c5002417f1fa91d923e6e104de6921e36cd7@localhost:30303' }" +
//                "] \n" +
//                "sync.enabled = true \n" +
//                // all peers in the same network need to use the same genesis block
//                "genesis = genesis-light-sb.json \n" +
////                "blockchain.config.name = 'testnet' \n" +
//                // two peers need to have separate database dirs
//                "database.dir = confContratoNodeRegular \n" +
//        		"cache.flush.memory = 0";
    	
//        private final String config =
//                // no discovery: we are connecting directly to the miner peer
//                "peer.discovery.enabled = false \n" +
//                "peer.listen.port = 30341 \n" +
////                "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n" +
//                "peer.networkId = 1 \n" +
//                // actively connecting to the miner
//                "peer.active = [" +
//                "    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30340' }, " +
//                "    { url = 'enode://960d947d1c6a4c3413f8626e3f9b406b38a092fa13e3b286657996265a16c147dab1237a418ee366dcc1f3bf4cf3bbc8890ff9430a75dce63d69898b8ffb89f0@localhost:30303' }" +
//                "] \n" +
//                "sync.enabled = true \n" +
//                // all peers in the same network need to use the same genesis block
//                "genesis = genesis-light-sb.json \n" +
////                "blockchain.config.name = 'testnet' \n" +
//                // two peers need to have separate database dirs
//                "database.dir = confContratoNodeRegular \n" +
//        		"cache.flush.memory = 0";
//        
    

        public abstract ConfConexionPrivada sampleBean();

        @Bean
        public SystemProperties systemProperties() {
            SystemProperties props = new SystemProperties();
            props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
            return props;
        }
    }

    @Override
    public void onSyncDone() throws Exception {
        super.onSyncDone();
    }

    public static void main(String[] args) throws Exception {
        sLogger.info("Starting EthereumJ!");

        class SampleConfig extends TestNetConfig {
            @Bean
            public ConfConexionPrivada sampleBean() {
                return new ConfConexionPrivada();
            }
        }

        // Based on Config class the BasicSample would be created by Spring
        // and its springInit() method would be called as an entry point
        EthereumFactory.createEthereum(SampleConfig.class);
    }
}
