package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;

import io.coti.cotinode.data.TransactionData;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Configurable
public class TccConfirmationService {
    private final int TRESHOLD = 10;
    private Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private ConcurrentHashMap<Hash, TransactionData> hashToUnTccConfirmTransactionsMapping;
    private LinkedList<TransactionData> result;

    public void init(ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping) {
        hashToUnTccConfirmTransactionsMapping = new ConcurrentHashMap<>();
        for (Map.Entry<Hash, TransactionData> entry : hashToUnConfirmationTransactionsMapping.entrySet()) {
            if (!entry.getValue().isTransactionConsensus()) {
                hashToUnTccConfirmTransactionsMapping.put(entry.getValue().getHash(), entry.getValue());
            }
        }
    }

    //takes adjacency list of a directed acyclic graph(DAG) as input
    //returns a linkedlist which consists the vertices in topological order
    public void topologicalSorting() {
        result = new LinkedList<TransactionData>();

        // Reset
        for (Map.Entry<Hash, TransactionData> entry : hashToUnTccConfirmTransactionsMapping.entrySet()) {
            entry.getValue().setVisit(false);
        }

        //loop is for making sure that every vertex is visited since if we select only one random source
        //all vertices might not be reachable from this source
        //eg:1->2->3,1->3 and if we select 3 as source first then no vertex can be visited ofcourse except for 3
        for (Map.Entry<Hash, TransactionData> entry : hashToUnTccConfirmTransactionsMapping.entrySet()) {
            if (!entry.getValue().isVisit()) {
                topologicalSortingHelper(entry.getValue());
            }
        }
    }

    private void setTotalSumScore(TransactionData parent) {
        double maxSonsTotalTrustScore = 0;
        Hash maxSonsTotalTrustScoreHash = null;
        for (Hash hash : parent.getChildrenTransactions()) {
            try {
                if (hashToUnTccConfirmTransactionsMapping.get(hash).getTrustChainTrustScore()
                        > maxSonsTotalTrustScore) {
                    maxSonsTotalTrustScore = hashToUnTccConfirmTransactionsMapping.get(hash).getTrustChainTrustScore();
                    maxSonsTotalTrustScoreHash = hash;
                }
            } catch (Exception e) {
                log.error("in setTotalSumScore: parent: {} child: {}", parent.getHash(), hash );
                throw e;
            }
        }

        // updating parent trustChainTrustScore
        parent.setTrustChainTrustScore(parent.getSenderTrustScore() + maxSonsTotalTrustScore);

        //updating parent trustChainTransactionHashes
        if (maxSonsTotalTrustScoreHash != null) { // not a source
            List<Hash> maxSonsTotalTrustScoreChain =
                    new Vector<>(hashToUnTccConfirmTransactionsMapping.get(maxSonsTotalTrustScoreHash).getTrustChainTransactionHash());
            maxSonsTotalTrustScoreChain.add(maxSonsTotalTrustScoreHash);
            parent.setTrustChainTransactionHash(maxSonsTotalTrustScoreChain);
        }
    }

    private void topologicalSortingHelper(TransactionData parentTransactionData) {
        for (Hash transactionDataHash : parentTransactionData.getChildrenTransactions()) {
            TransactionData childTransactionData = hashToUnTccConfirmTransactionsMapping.get(transactionDataHash);
            if (!childTransactionData.isVisit()) {
                topologicalSortingHelper(childTransactionData);
            }

        }
        //making the vertex visited for future reference
        parentTransactionData.setVisit(true);

        //pushing to the stack as departing
        result.addLast(parentTransactionData);
    }

    public List<Hash> setTransactionConsensus() {
        List<Hash> transactionConsensusConfirmed = new Vector<>();
        for(TransactionData transaction : result ) {
            setTotalSumScore(transaction);
            if (transaction.getTrustChainTrustScore() >= TRESHOLD) {
                transaction.setTransactionConsensus(true);
                transaction.setTransactionConsensusUpdateTime(new Date());
                transactionConsensusConfirmed.add(transaction.getHash());
                log.info("transaction with hash:{} is confirmed with trustScore: {} and totalTrustScore:{} ", transaction.getHash(),transaction.getSenderTrustScore(),  transaction.getTrustChainTrustScore());
                log.info("Trust Chain Transaction Hashes of transaction {}", Arrays.toString(transaction.getTrustChainTransactionHash().toArray()));
                for(Hash hash: transaction.getTrustChainTransactionHash()) {
                    log.info(hash.toString());
                }
                log.info("end of trust chain");
            }
        }

        return transactionConsensusConfirmed;
    }

}
