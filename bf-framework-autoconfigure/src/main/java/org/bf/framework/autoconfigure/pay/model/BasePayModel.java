package org.bf.framework.autoconfigure.pay.model;

import java.io.Serializable;

public class BasePayModel implements Serializable {
    /** 商户订单号 **/
    protected String payOrderId;

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }
}
