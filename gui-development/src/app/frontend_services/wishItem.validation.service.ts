import {Injectable} from '@angular/core';

@Injectable()
export default class WishItemValidationService {

  NAME_MIN_LENGTH = 2;
  NAME_MAX_LENGTH = 50;
  NAME_REG_EXP = '^[a-zA-Zá-žÁ-Ž\\w]+[a-zA-Zá-žÁ-Ž\\w\\s,.?!_\\-]{' + (this.NAME_MIN_LENGTH - 1) + ',' + this.NAME_MAX_LENGTH + '}$';

  PRICE_MIN_LENGTH = 1;
  PRICE_MAX_LENGTH = 11;
  PRICE_REG_EXP = '^\\d{' + this.PRICE_MIN_LENGTH + ',' + this.PRICE_MAX_LENGTH + '}$';

  DESCRIPTION_MAX_LENGTH = 500;
  DESCRIPTION_REG_EXP = '^[a-zA-Zá-žÁ-Ž\\w\\s,.?!_\\-]{0,' + this.DESCRIPTION_MAX_LENGTH + '}$';

  itemNameValidation(name) {
    let message = '';

    const tmpName = name.trim();
    if (!tmpName.match(this.NAME_REG_EXP)) {
      message = 'Item name contains illegal characters.';
    }

    if (name.replace('\\s', '').isEmpty || name.trim().length < this.NAME_MIN_LENGTH) {
      message = 'Item name must contain at least ' + this.NAME_MIN_LENGTH + ' characters.';
    }

    if (tmpName.length > this.NAME_MAX_LENGTH) {
      message = 'The item name can contain a maximum of ' + this.NAME_MAX_LENGTH + ' characters.';
    }
    return message;
  }

  itemPriceValidation(price) {
    let message = '';

    if (!price.toString().match(this.PRICE_REG_EXP)) {
      message = 'The price of the item contains illegal characters.';
    }

    if (price.toString().length === 0) {
      message = 'Price must be entered.';
    }

    if (price < 0) {
      message = 'Price must be a positive number';
    }
    return message;
  }

  itemDescriptionValidation(description) {
    let message = '';

    if (description === null || description === undefined) {
      return message;
    }

    let tmpDescription = description.toString().replace('\s', '');

    if (tmpDescription.length > 0) {
      tmpDescription = description.trim();
      if (!tmpDescription.match(this.DESCRIPTION_REG_EXP)) {
        message = 'The description contains illegal characters.';
      }
      if (tmpDescription.length > this.DESCRIPTION_MAX_LENGTH) {
        message = 'The description can contain a maximum of ' + this.DESCRIPTION_MAX_LENGTH + 'characters.';
      }
    }
    return message;
  }
}
