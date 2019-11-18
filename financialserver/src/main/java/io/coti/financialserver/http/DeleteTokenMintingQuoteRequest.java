package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class DeleteTokenMintingQuoteRequest extends Request implements ISignValidatable {

    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private @Valid Hash warrantFeeHash;
    @NotNull
    private @Valid Hash currencyHash;
    @NotNull
    private Instant requestTime;
    @NotNull
    private @Valid SignatureData signature;

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}
