package io.coti.basenode.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class NetworkFeeData extends OutputBaseTransactionData {
    @NotNull
    private Map<Hash,SignatureData> networkFeeTrustScoreNodeResult;

    public NetworkFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount,Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, originalAmount, baseTransactionHash, signature, createTime);
    }
}