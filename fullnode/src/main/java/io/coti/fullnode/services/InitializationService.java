package io.coti.fullnode.services;

import io.coti.common.data.NodeType;
import io.coti.common.services.BaseNodeInitializationService;
import io.coti.common.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class InitializationService {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;


    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        communicationService.initSender(receivingServerAddresses);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.FullNode);

        baseNodeInitializationService.init();
    }
}