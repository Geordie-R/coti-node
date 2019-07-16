package io.coti.historynode.services;

import io.coti.basenode.crypto.AddressesRequestCrypto;
import io.coti.basenode.crypto.AddressesResponseCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.basenode.http.GetHistoryAddressesResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.BaseNodeValidationService;
import io.coti.historynode.database.HistoryRocksDBConnector;
import io.coti.historynode.http.GetAddressBatchResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import utils.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@ContextConfiguration(classes = {AddressService.class, Addresses.class, HistoryRocksDBConnector.class, NodeCryptoHelper.class,
        AddressesRequestCrypto.class, AddressesResponseCrypto.class, StorageConnector.class, BaseNodeValidationService.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class AddressServiceIntegrationTest {

    public static final int NUMBER_OF_ADDRESSES = 8;

    @Autowired
    private AddressService addressService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private HistoryRocksDBConnector historyRocksDBConnector;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private Addresses addressesCollection;
    @Autowired
    private AddressesRequestCrypto addressesRequestCrypto;
    @Autowired
    private AddressesResponseCrypto addressesResponseCrypto;

    @MockBean
    private BaseNodeValidationService baseNodeValidationService;

    private GetHistoryAddressesRequest getHistoryAddressesRequest;

    private Hash insertedHash;

    @Before
    public void setUp() throws Exception {
        databaseConnector.init();
        getHistoryAddressesRequest = TestUtils.generateGetAddressesRequest();
        addressesRequestCrypto.signMessage(getHistoryAddressesRequest);
        insertedHash = getHistoryAddressesRequest.getAddressesHash().iterator().next();
        insertAddressDataToRocksDB(insertedHash);
    }

    @After
    public void finishUp() throws Exception {
        addressesCollection.deleteByHash(insertedHash);
    }

    @Test
    public void getAddressesTestTemp(){
        addressService.getAddresses(getHistoryAddressesRequest);
    }


    //TODO 7/14/2019 astolia: unit test
    @Test
    public void testBadSignature() {
            getHistoryAddressesRequest = TestUtils.generateGetAddressesRequest();
            getHistoryAddressesRequest.setSignerHash(new Hash(1));
            getHistoryAddressesRequest.setSignature(new SignatureData());
            ResponseEntity<GetHistoryAddressesResponse> response = addressService.getAddresses(getHistoryAddressesRequest);
            Assert.assertEquals(HttpStatus.UNAUTHORIZED,response.getStatusCode());
            Assert.assertEquals(BaseNodeHttpStringConstants.INVALID_SIGNATURE,response.getBody().getMessage());
            Assert.assertEquals(BaseNodeHttpStringConstants.STATUS_ERROR,response.getBody().getStatus());
    }

    @Test
    public void storePlusRetrieveAddress_AddressMatch()
    {
        // This is an integration test, requiring Storage Node to be up as well.

        List<Hash> addresses = new ArrayList<>();
        List<AddressData> addressesData = new ArrayList<>();
        IntStream.range(0, NUMBER_OF_ADDRESSES).forEachOrdered(n -> {
            Hash hash = TestUtils.generateRandomHash();
            addresses.add(hash);
            addressesData.add(new AddressData(hash));
        });

        ResponseEntity<IResponse> response = ResponseEntity
                .status(HttpStatus.OK)
                .body(new GetAddressBatchResponse(addressesData));

        //TODO: not finished
// This currently fails
    }

    private void insertAddressDataToRocksDB(Hash addressHash){
        AddressData data = new AddressData(addressHash);
        addressesCollection.put(data);
    }

}