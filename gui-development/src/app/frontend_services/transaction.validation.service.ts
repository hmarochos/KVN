import {Injectable} from '@angular/core';

@Injectable()
export default class TransactionValidationService {

  DESCRIPTION_MAX_LENGTH = 500;
  DESCRIPTION_REG_EXP = '^[a-zA-Zá-žÁ-Ž\\w\\s,.?!_\\-]{0,' + (this.DESCRIPTION_MAX_LENGTH - 1) + '}$';
  NUMBER_REG_EX = '\\d+';

  ACCOUNTID_MIN_LENGTH = 3;
  ACCOUNTID_MAX_LENGTH = 15;
  ACCOUNTID_REGEX = '^[\\d]+\\s*$';

  validateAmount(amount) {
    let message = '';
    if (amount === null || amount === undefined) {
      message = 'the amount must be entered.';
      return message;
    }
    if (amount < 0) {
      message = 'The amount must be positive.';
    }
    if (!amount.match(this.NUMBER_REG_EX)) {
      message = 'Enter only numbers.';
    }
    return message;
  }

  validateBankCode(bankCode) {
    let message = '';
    if (bankCode === null || bankCode === undefined) {
      message = 'bank code must be entered.';
      return message;
    }
    if (bankCode < 0) {
      message = 'bank code cannot be a negative number.';
    }
    if (!bankCode.match(this.NUMBER_REG_EX)) {
      message = 'Enter only numbers.';
    }
    return message;
  }

  validatePartyDescription(partyDescription) {
    let message = '';
    if (partyDescription == null || partyDescription.replace('\\s', '').isEmpty || partyDescription.length === 0) {
      message = 'The transaction description must be filled out.';
      return message;
    }
    const tmpPartyDescription = partyDescription.trim();
    if (tmpPartyDescription.length > this.DESCRIPTION_MAX_LENGTH) {
      message = 'The payment description can contain a maximum of' + this.DESCRIPTION_MAX_LENGTH + ' znaků.';
    }
    if (!tmpPartyDescription.match(this.DESCRIPTION_REG_EXP)) {
      message = 'Payment description contains illegal characters.';
    }
    return message;
  }

  validateSymbols(symbol) {
    let message = '';
    if (symbol === null || symbol === undefined) {
      return message;
    }
    if (!symbol.match(this.NUMBER_REG_EX)) {
      message = 'Symbols can only contain numbers.';
    }
    return message;
  }

  accountIdValidation(accountId): string {
    let message = '';

    if (accountId === undefined || accountId === null) {
      return 'Account number must be filled out.';
    }

    const tmpAccountId = accountId.toString().replace('\s', '');

    if (tmpAccountId.length === 0) {
      return message;
    } else if (tmpAccountId.length < this.ACCOUNTID_MIN_LENGTH) {// Not necessary
      message = 'Account number must contain at least ' + this.ACCOUNTID_MIN_LENGTH + ' numbers.';
    } else if (tmpAccountId.length > this.ACCOUNTID_MAX_LENGTH) {
      message = 'Account number can contain a maximum of ' + this.ACCOUNTID_MAX_LENGTH + ' numbers.';
    } else if (!tmpAccountId.match(this.ACCOUNTID_REGEX)) {
      message = 'Account number contains illegal characters.';
    }
    return message;
  }
}
