package me.xuan.bdocr.sdk.model;

/**
 * Author: xuan
 * Created on 2019/10/23 15:12.
 * <p>
 * Describe:
 */
public class BankCardResult extends ResponseResult {
    private String bankCardNumber;
    private String bankName;
    private String validDate;
    private BankCardType bankCardType;

    public BankCardResult() {
    }

    public String getValidDate() {
        return validDate;
    }

    public void setValidDate(String validDate) {
        this.validDate = validDate;
    }

    public String getBankName() {
        return this.bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BankCardType getBankCardType() {
        return this.bankCardType;
    }

    public void setBankCardType(BankCardType bankCardType) {
        this.bankCardType = bankCardType;
    }

    public void setBankCardType(int bankCardTypeId) {
        this.bankCardType = BankCardType.FromId(bankCardTypeId);
    }

    public String getBankCardNumber() {
        return this.bankCardNumber;
    }

    public void setBankCardNumber(String bankCardNumber) {
        this.bankCardNumber = bankCardNumber;
    }

    public boolean isRecCorrect() {
        return this.bankCardType != BankCardType.Unknown;
    }

    public static enum BankCardType {
        Unknown(0),
        Debit(1),
        Credit(2);

        private final int id;

        private BankCardType(int id) {
            this.id = id;
        }

        public static BankCardType FromId(int id) {
            switch (id) {
                case 1:
                    return Debit;
                case 2:
                    return Credit;
                default:
                    return Unknown;
            }
        }
    }
}