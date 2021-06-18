import {Component} from '@angular/core';
import UserValidationService from '../frontend_services/user.validation.service';
import UserService from '../backend_services/user.service';
import {ToastrService} from 'ngx-toastr';


@Component({
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  providers: [UserValidationService, UserService, ToastrService]
})


export default class RegisterComponent {

  password;
  pass1;
  statusText: string;
  registrationInfo = [];
  objectReg;
  loading = false;
  toastSetting = {progressBar: true, positionClass: 'toast-top-right', timeOut: 6000};
  responseMessage = {status: 0, message: ''};

  // Šlo by i bez deklarace zde, ale pro přehlednost volím tento způsob

  regexFirstName = this._userValidationService.FIRST_NAME_REGEX;
  regexLastName = this._userValidationService.LAST_NAME_REGEX;
  regexEmail = this._userValidationService.EMAIL_REGEX;
  regexPass = this._userValidationService.PASSWORD_REGEX;
  regexTel = this._userValidationService.TELEPHONE_REGEX;
  regexStreet = this._userValidationService.ADDRESS_STREET_REGEX;
  regexCity = this._userValidationService.ADDRESS_CITY_REGEX;
  regexPsc = this._userValidationService.ADDRESS_PSC_REGEX;
  regexAccountId = this._userValidationService.ACCOUNTID_REGEX;

  firstNameMinLength = this._userValidationService.FIRST_NAME_MIN_LENGTH;
  firstNameMaxLength = this._userValidationService.FIRST_NAME_MAX_LENGTH;
  lastNameMinLength = this._userValidationService.LAST_NAME_MIN_LENGTH;
  lastNameMaxLength = this._userValidationService.LAST_NAME_MAX_LENGTH;
  emailMinLength = this._userValidationService.EMAIL_MIN_LENGTH;
  emailMaxLength = this._userValidationService.EMAIL_MAX_LENGTH;
  passwordMinLength = this._userValidationService.PASSWORD_MIN_LENGTH;
  passwordMaxLength = this._userValidationService.PASSWORD_MAX_LENGTH;
  telMinLength = this._userValidationService.TELEPHONE_MIN_LENGTH;
  telMaxLength = this._userValidationService.TELEPHONE_MAX_LENGTH;
  cityMinLength = this._userValidationService.ADDRESS_CITY_MIN_LENGTH;
  cityMaxLength = this._userValidationService.ADDRESS_CITY_MAX_LENGTH;
  streetMinLength = this._userValidationService.ADDRESS_STREET_MIN_LENGTH;
  streetMaxLength = this._userValidationService.ADDRESS_STREET_MAX_LENGTH;
  pscMinLength = this._userValidationService.ADDRESS_PSC_MIN_LENGTH;
  pscMaxLength = this._userValidationService.ADDRESS_PSC_MAX_LENGTH;
  accountIdMinLength = this._userValidationService.ACCOUNTID_MIN_LENGTH;
  accountIdMaxLength = this._userValidationService.ACCOUNTID_MAX_LENGTH;

  constructor(private _userValidationService: UserValidationService, private _userService: UserService,
              private _toastService: ToastrService) {
  }



  getUserForm(user) {
    if (this.inputValidation(user).length === 0) {

      this.objectReg = {
        firstName: user.value.firstName,
        lastName: user.value.lastName,
        email: user.value.email,
        password: user.value.password,
        passwordConfirmation: user.value.pass1,
        accountId: this._userService.setNullData(user.value.accountId),
        telephoneNumber: user.value.telephoneNumber,
        address:
          {
            street: user.value.street,
            city: user.value.city,
            psc: user.value.psc
          }
      };
      this.responseMessage = {status: 0, message: ''};
      this.loading = true;
      this._userService.postUserRegistration(this.objectReg)
        .then(() => {
          this.loading = false;
          this.registrationResponseOK();
          user.reset();
        }).catch(err => {
        this.password = '';
        this.pass1 = '';
        this.loading = false;
        if (err.response !== undefined) {
          if (err.response.data.message === 'Email already used.') {
            this.registrationResponseErrorMail();
          } else {
            this.registrationResponseErrorMessage(err.response.data.message);
          }
        } else {
          this.serverRegistrationError();
        }
      });
    }
  }


  inputValidation(user) {
    this.registrationInfo = [];
    let message;
    message = this._userValidationService.firstNameValidation(user.value.firstName);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.lastNameValidation(user.value.lastName);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.emailValidation(user.value.email);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.passwordValidation(user.value.password);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.telephoneNumberValidation(user.value.telephoneNumber);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.streetValidation(user.value.street);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.cityValidation(user.value.city);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.pscValidation(user.value.psc);
    if (message !== '') {
      this.registrationInfo.push(message);
    }
    message = this._userValidationService.accountIdValidation(user.value.accountId);
    if (message !== '') {
      this.registrationInfo.push(message);
    }

    if (user.value.pass1 !== user.value.password) {
      message = 'Zadané hesla se neshodují.';
      this.registrationInfo.push(message);
    }
    return this.registrationInfo;
  }


  registrationResponseOK() {
    this._toastService.success('Registrace proběhla v pořádku:', 'Registrace úspěšná:', this.toastSetting);
    this.responseMessage = {status: 1, message: 'Registration went well.'};
  }


  registrationResponseErrorMail() {
    this._toastService.error('Tento email je již používán.:', 'Error registrace:', this.toastSetting);
    this.responseMessage = {status: -1, message: 'Tento email je již používán.'};
  }


  serverRegistrationError() {
    this._toastService.error('An error occurred on the server.', 'Error:', this.toastSetting);
    this.responseMessage = {status: -1, message: 'Sorry, an error occurred in the north, please try again later.'};
  }


  registrationResponseErrorMessage(message) {
    this._toastService.error(message, 'Error:', this.toastSetting);
    this.responseMessage = {status: -1, message: message};
  }
}
