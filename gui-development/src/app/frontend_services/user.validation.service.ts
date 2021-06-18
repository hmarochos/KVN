import {Injectable} from '@angular/core';

// TODO complete validation (whitespace ...)
@Injectable()
export default class UserValidationService {

  FIRST_NAME_MIN_LENGTH = 3;
  FIRST_NAME_MAX_LENGTH = 30;
  FIRST_NAME_REGEX = '^[A-ZÁ-Ž][a-zA-ZÁ-Ž]+\\s*$';

  LAST_NAME_MIN_LENGTH = 4;
  LAST_NAME_MAX_LENGTH = 50;
  LAST_NAME_REGEX = '^[A-ZÁ-Ž]+[a-zA-ZÁ-Ž]+\\s*$';

  EMAIL_MIN_LENGTH = 10;
  EMAIL_MAX_LENGTH = 150;
  EMAIL_REGEX = '^[\\w\\-+]+(\\.[\\w\\-]+)*@[A-Za-z\\d\\-]+(\\.[A-Za-z\\d]+)*(\\.[A-Za-z]{2,})$';

  PASSWORD_MIN_LENGTH = 8;
  PASSWORD_MAX_LENGTH = 50;
  PASSWORD_REGEX = '^(?=.*[\\d])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=¨ˇ!/\\\\*|\'-;_():<>{}\\[\\]])(?=\\S+$).{8,50}$';

  TELEPHONE_REGEX = '^(\\+\\d{12}$|^\\d{9})$|(\\d{3}\\s*\\d{3}\\s*\\d{3}|\\+\\s*\\d{3}\\s*\\d{3}\\s*\\d{3}\\s*\\d{3})\\s*$';
  TELEPHONE_MIN_LENGTH = 9;
  TELEPHONE_MAX_LENGTH = 30;

  ACCOUNTID_MIN_LENGTH = 3;
  ACCOUNTID_MAX_LENGTH = 15;
  ACCOUNTID_REGEX = '^[\\d]+\\s*$';

  ADDRESS_STREET_MIN_LENGTH = 3;
  ADDRESS_STREET_MAX_LENGTH = 100;
  ADDRESS_STREET_REGEX = '^([a-zA-Zá-žÁ-Ž]+|[a-zA-Zá-žÁ-Ž]+\\s[a-zA-Zá-žÁ-Ž]+|[a-zA-Zá-žÁ-Ž]+[\\s+a-zA-Zá-žÁ-Ž]+)\\s+\\d+\\s*$';

  ADDRESS_CITY_MIN_LENGTH = 2;
  ADDRESS_CITY_MAX_LENGTH = 100;
  ADDRESS_CITY_REGEX = '^([a-zA-Zá-žÁ-Ž]+|[a-zA-Zá-žÁ-Ž]+\\s+[a-zA-Zá-žÁ-Ž]+|[a-zA-Zá-žÁ-Ž]+[\\s+a-zA-Zá-žÁ-Ž]+)$';

  ADDRESS_PSC_REGEX = '^\\d{3}\\s*\\d{2}\\s*';
  ADDRESS_PSC_MIN_LENGTH = 5;
  ADDRESS_PSC_MAX_LENGTH = 6;


  firstNameValidation(firstName): string {
    let message = '';
    const tmpFirstName = firstName.trim();

    if (!tmpFirstName.match(this.FIRST_NAME_REGEX)) {
      message = 'The name contains illegal characters. It must start with a capital letter.';
    }
    if (firstName.replace('\s', '').isEmpty || firstName.trim().length < this.FIRST_NAME_MIN_LENGTH) {
      message = 'The name must contain at least ' + this.FIRST_NAME_MIN_LENGTH + ' characters.';
    }
    if (tmpFirstName.length > this.FIRST_NAME_MAX_LENGTH) {
      message = 'The name can contain a maximum of ' + this.FIRST_NAME_MAX_LENGTH + ' characters.';
    }
    return message;
  }


  lastNameValidation(lastName): string {
    let message = '';
    const tmpLastName = lastName.trim();

    if (!tmpLastName.match(this.LAST_NAME_REGEX)) {
      message = 'Last name contains illegal characters. It must start with a capital letter.';
    }
    if (lastName.replace('\s', '').isEmpty || lastName.trim().length < this.LAST_NAME_MIN_LENGTH) {
      message = 'Last name must contain at least ' + this.LAST_NAME_MIN_LENGTH + ' characters.';
    }
    if (tmpLastName.length > this.LAST_NAME_MAX_LENGTH) {
      message = 'Last name can be a maximum of ' + this.LAST_NAME_MAX_LENGTH + ' characters.';
    }
    return message;
  }


  emailValidation(email): string {
    let message = '';
    const tmpEmail = email.trim();

    if (!tmpEmail.match(this.EMAIL_REGEX)) {
      message = 'Email contains illegal characters or is entered in the wrong format.';
    }
    if (email.replace('\s', '').isEmpty || email.trim().length < this.EMAIL_MIN_LENGTH) {
      message = 'Email must contain at least ' + this.EMAIL_MIN_LENGTH + ' characters.';
    }
    if (tmpEmail.length > this.EMAIL_MAX_LENGTH) {
      message = 'Email can contain a maximum of ' + this.EMAIL_MAX_LENGTH + ' characters.';
    }
    return message;
  }


  passwordValidation(password): string {
    let message = '';
    const tmpPassword = password.trim();

    if (!tmpPassword.match(this.PASSWORD_REGEX)) {
      message = 'Password must contain at least 1 number, lowercase letter, uppercase letter, ' +
        'and a special character, such as \'@#$%^&+=!/\\*|\'-;_():<>{}[]\' etc..';
    }
    if (password.replace('\s', '').isEmpty || password.replace('\s', '').length < this.PASSWORD_MIN_LENGTH) {
      message = 'Password must contain at least ' + this.PASSWORD_MIN_LENGTH + ' characters.';
    }
    if (tmpPassword.length > this.PASSWORD_MAX_LENGTH) {
      message = 'Password can contain a maximum of ' + this.PASSWORD_MAX_LENGTH + ' characters.';
    }
    return message;
  }


  accountIdValidation(accountId): string {
    let message = '';

    if (accountId === undefined || accountId === null) {
      return message;
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


  telephoneNumberValidation(telephoneNumber): string {
    let message = '';

    if (telephoneNumber === undefined || telephoneNumber === null) {
      return message;
    }

    const tmpTelephoneNumber = telephoneNumber.toString().replace('\s', '');

    if (tmpTelephoneNumber.toString().length > 0) {
      if (!tmpTelephoneNumber.match(this.TELEPHONE_REGEX)) {
        message = 'The telephone number can be entered either with or without a prefix. That is 9 or 13 characters.';
      }
    }

    return message;
  }


  cityValidation(city): string {
    let message = '';
    const tmpCity = city.toString().trim();

    if (!tmpCity.match(this.ADDRESS_CITY_REGEX)) {
      message = 'The city is entered in the wrong format.';
    }
    if (tmpCity.length < this.ADDRESS_CITY_MIN_LENGTH) {
      message = 'The city must contain at least ' + this.ADDRESS_CITY_MIN_LENGTH + ' characters.';
    }
    if (tmpCity.length > this.ADDRESS_CITY_MAX_LENGTH) {
      message = 'The city can contain a maximum of ' + this.ADDRESS_CITY_MAX_LENGTH + ' characters.';
    }
    return message;
  }


  streetValidation(street): string {
    let message = '';
    const tmpStreet = street.trim();

    if (!tmpStreet.match(this.ADDRESS_STREET_REGEX)) {
      message = 'The street is entered in the wrong format.';
    }
    if (street.trim().length < this.ADDRESS_STREET_MIN_LENGTH) {
      message = 'The street must contain at least ' + this.ADDRESS_STREET_MIN_LENGTH + ' characters.';
    }
    if (tmpStreet.length > this.ADDRESS_STREET_MAX_LENGTH) {
      message = 'The street can contain a maximum of ' + this.ADDRESS_STREET_MAX_LENGTH + ' characters.';
    }
    return message;
  }


  pscValidation(psc): string {
    let message = '';

    if (psc.toString().length === 0) {
      message = 'Zip code must be entered.';
    } else if (!psc.toString().replace('\s', '').match(this.ADDRESS_PSC_REGEX)) {
      message = 'Postal code contains illegal characters. Please enter a valid zip code.';
    }
    return message;

  }

}
