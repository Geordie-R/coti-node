package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.RollingReserveData;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.RollingReserveReleaseDateData;
import io.coti.financialserver.data.RollingReserveReleaseStatus;
import io.coti.financialserver.model.RollingReserveReleaseDates;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Data
public class GetRollingReserveReleaseDatesResponse extends BaseResponse {

    String merchantHashString;
    String rollingReserveAddress;
    Map<String, RollingReserveReleaseStatus> rollingReserveReleases;

    @Autowired
    RollingReserveReleaseDates rollingReserveReleaseDates;

    public GetRollingReserveReleaseDatesResponse(RollingReserveData rollingReserveData, Map<String, RollingReserveReleaseStatus> rollingReserveReleases) {
        super();

        this.rollingReserveReleases = rollingReserveReleases;

        Hash merchantHash = rollingReserveData.getMerchantHash();
        merchantHashString = merchantHash.toString();
        rollingReserveAddress = rollingReserveData.getRollingReserveAddress().toString();


    }
}