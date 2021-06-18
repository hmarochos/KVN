import {Component, OnInit} from '@angular/core';
import UserService from '../backend_services/user.service';
import {ToastrService} from 'ngx-toastr';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {Router} from '@angular/router';
import UserValidationService from '../frontend_services/user.validation.service';
import TokenService from '../backend_services/tokenService';


@Component({
  templateUrl: './profile.component.html',
  providers: [UserService, UserService, CookieService, UserValidationService, TokenService],
  styleUrls: ['./profile.component.css'],
})
export default class ProfileComponent implements OnInit {

  userProfile;
  userProfileDefault;
  loading: boolean;
  responseMessage: string;
  editProfile = false;
  toastSetting = {progressBar: true, positionClass: 'toast-top-right', timeOut: 5000};
  serverResponse: string;
  serverResponseStatus;
  serverChangeResponse: string;
  serverChangeResponseStatus;
  showChangePassword = false;
  validationInfo = [];

  constructor(private _router: Router, private _userService: UserService, private _toastService: ToastrService,
              private _cookieService: CookieService, private _userValidationService: UserValidationService,
              private _tokenService: TokenService) {
  }

  // GETPROFILE START
  ngOnInit() {
    if (!this._cookieService.get('accessToken')) {
      return this._router.navigate(['unautentized']);
    } else {
      this.serverGetUser();
    }
  }


  serverGetUser() {
    if (!this._cookieService.get('accessToken')) {
      return this._router.navigate(['unautentized']);
    } else {
      this.loading = true;
      this._userService.getUserProfile()
        .then(response => {
          this.serverGetResponseOk(response.data.body);
        })
        .catch(err => {
          if (err.response !== undefined) {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.serverGetUser();
                } else {
                  this.tokenIsInvalid()
                }
              })
              .catch(err => {
                console.log(err);
                this.tokenIsInvalid()
              })
          } else {
            this.serverGetError();
          }
        });
    }
  }


  serverGetResponseOk(response) {
    this.userProfile = response;
    this.userProfileDefault = {
      old_firstName: response.firstName,
      old_lastName: response.lastName,
      old_email: response.email,
      old_telephoneNumber: response.telephoneNumber,
      old_accountId: response.accountId,
      old_city: response.address.city,
      old_street: response.address.street,
      old_psc: response.address.psc
    };
    this.loading = false;
  }


  tokenIsInvalid() {
    this._toastService.error('Pro přístup je nutné se znovu přihlásit.', 'Přihlášení vypršelo:', this.toastSetting);
    this.loading = false;
    this._cookieService.delete('accessToken');
    return this._router.navigate(['/unautentized']);
  }


  serverGetError() {
    this._toastService.error('An error occurred on the server.', 'Error:', this.toastSetting);
    this.loading = false;
    this.responseMessage = 'Sorry, an error occurred in the north, please try again later.';
  }

// GETPROFILE END


  // UpdateProfile START
  editProfileClicked() {
    this.inputSetDefault();
    this.editProfile = !this.editProfile;
  }


  inputSetDefault() {
    if (this.editProfile) {
      this.userProfile = {
        firstName: this.userProfileDefault.old_firstName,
        lastName: this.userProfileDefault.old_lastName,
        email: this.userProfileDefault.old_email,
        telephoneNumber: this.userProfileDefault.old_telephoneNumber,
        accountId: this.userProfileDefault.old_accountId,
        address: {
          city: this.userProfileDefault.old_city,
          street: this.userProfileDefault.old_street,
          psc: this.userProfileDefault.old_psc
        }
      };
    }
  }


  saveProfileClicked() {
    this.serverResponse = '';
    if (this.validateInputs(this.userProfile).length === 0) {
      this.loading = true;
      this._userService.putUpdateUser(this.userProfile).then(() => {
        this.loading = false;
        this.editProfile = !this.editProfile;
        this.serverUpdateResponseOk();
        this.serverGetUser();
      })
        .catch(err => {
          this.loading = false;
          if (err.response !== undefined) {
            if (err.response.data.message !== undefined) {
              this.serverUpdateErrorMessage();
            } else {
              this._tokenService.handleRefreshToken()
                .then(resp => {
                  if (resp) {
                    this.saveProfileClicked();
                  } else {
                    this.tokenIsInvalid()
                  }
                })
                .catch(err => {
                  console.log(err);
                  this.tokenIsInvalid()
                })
            }
          } else {
            this.serverGetError();
          }
        });
    }
  }


  validateInputs(userProfile) {
    this.validationInfo = [];
    let message;
    message = this._userValidationService.firstNameValidation(userProfile.firstName);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._userValidationService.lastNameValidation(userProfile.lastName);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._userValidationService.telephoneNumberValidation(userProfile.telephoneNumber);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._userValidationService.accountIdValidation(userProfile.accountId);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._userValidationService.cityValidation(userProfile.address.city);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._userValidationService.streetValidation(userProfile.address.street);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._userValidationService.pscValidation(userProfile.address.psc);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    return this.validationInfo;
  }


  serverUpdateErrorMessage() {
    this._toastService.error('Zkontrolujte zadané údaje, zda splňují veškeré náležitosti', 'Zadané údaje:', this.toastSetting);
    this.serverResponseStatus = -1;
    this.serverResponse = 'Zkontrolujte zadané údaje, zda splňují veškeré náležitosti a podléhají správnému formátu.';
  }


  serverUpdateResponseOk() {
    this._toastService.success('Údaje byly úspěšně změněny', 'Změněno:', this.toastSetting);
    this.serverResponseStatus = 1;
    this.serverResponse = 'Data byly úspěšně změněny.';
  }

  // UpdateProfile END


  // DeleteAccount Start
  serverDeleteAccount() {
    this.serverResponse = '';
    if (confirm('Opravdu chcete odstranit váš účet? Výsledek je nevratný!')) {
      this.loading = true;
      this._userService.deleteUser(this.userProfile.email).then(() => {
        this.loading = false;
        this.serverDeleteResponseOk();
      })
        .catch(err => {
          this.loading = false;
          if (err.response !== undefined) {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.serverDeleteAccount();
                } else {
                  this.tokenIsInvalid()
                }
              })
              .catch(err => {
                console.log(err);
                this.tokenIsInvalid()
              })
          } else {
            this.serverGetError();
          }
        });
    }
  }


  serverDeleteResponseOk() {
    this.serverResponseStatus = 1;
    this.serverResponse = 'Váš účet byl úspěšně smazán.';
    this._toastService.success('Váš účet byl úspěšně smazán', 'Odstranění úspěšné:', this.toastSetting);
    this._cookieService.delete('accessToken');
    this._cookieService.delete('refreshToken');
    this._cookieService.delete('remember');
    this._cookieService.delete('validity');
    setTimeout(() => {
        window.location.replace('/');
      },
      4000);
  }

  // DeleteAccount END

  // ChangePassword START
  // Vracení z Child komponenty
  functionGetChildInputsPasswords(childData) {
    this.serverChangePassword(childData);
  }


  serverChangePassword(pwData) {
    this.serverChangeResponse = '';
    this.loading = true;
    this._userService.changeUserPassword(pwData).then(() => {
      this.loading = false;
      this.serverChangePasswordResponseOk();
    }).catch(err => {
      this.loading = false;
      if (err.response !== undefined) {
        this._tokenService.handleRefreshToken()
          .then(resp => {
            if (resp) {
              this.serverChangePasswordUserError();
            } else {
              this.tokenIsInvalid()
            }
          })
          .catch(err => {
            console.log(err);
            this.tokenIsInvalid()
          })
      } else {
        this.serverGetError();
      }
    });
  }


  serverChangePasswordResponseOk() {
    this._toastService.success('Změna proběhla úspěšně.', 'Změna hesla:');
    this.serverChangeResponse = 'Změna hesla proběhla úspěšně.';
    this.serverChangeResponseStatus = 1;
    setTimeout(() => {
        this.showChangePassword = false;
      },
      1500);
  }


  serverChangePasswordUserError() {
    this._toastService.error('Zkontrolujte, zda zadáváte správné heslo.', 'Error:');
    this.serverChangeResponse = 'Zkontrolujte, zda zadáváte správné heslo. Změna nebyla provedena.';
    this.serverChangeResponseStatus = -1;
  }

  // ChangePasssword END
}


