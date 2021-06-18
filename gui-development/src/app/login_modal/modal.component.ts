import {Component, OnInit} from '@angular/core';
import UserService from '../backend_services/user.service';
import {ToastrService} from 'ngx-toastr';
import UserValidationService from '../frontend_services/user.validation.service';
import {CookieService} from 'ngx-cookie-service/dist-lib';


@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css'],
  providers: [CookieService, UserValidationService, ToastrService, UserService]
})

export class ModalComponent {
  statusText: string;
  statusState: number;
  signInInfo = [];
  remember = this._cookieService.get('remember') === 'yes';
  username: string;
  password: string;


  constructor(private _userService: UserService, private _toastrService: ToastrService,
              private _userValidationService: UserValidationService,
              private _cookieService: CookieService) {
  }


  signInValidation(userLogin) {
    this.signInInfo = [];
    let message;
    message = this._userValidationService.emailValidation(userLogin.value.username);
    if (message !== '') {
      this.signInInfo.push(message);
    }
    message = this._userValidationService.passwordValidation(userLogin.value.password);
    if (message !== '') {
      this.signInInfo.push(message);
    }
    return this.signInInfo;
  }


  doSign(userLogin) {
    if (this.signInValidation(userLogin).length === 0) {
      this.statusState = 1;
      this.statusText = 'Please wait...';

      this._userService.signInUser(userLogin.value)
        .then(response => {
          this.signInSuccess(response, userLogin);
        })
        .catch(err => {
          if (err.response) {
            this.signInFailed(err.response.status);
          } else {
            this.serverError();
          }
        });
    }
  }


  signInSuccess(response, userLogin) {
    this.statusText = 'Vítejte, jste přihlášen.';
    this.statusState = 2;
    this._cookieService.set('accessToken', response.data.access_token);
    this.rememberUser(userLogin, response);
    this._toastrService.success('Vítejte!', 'Jste přihlášen.');
    this.finishSign();
  }


  signInFailed(respStatusCode) {
    this.password = '';
    if (respStatusCode === 400) {
      this._toastrService.error('Error!', 'Zadané údaje jsou špatné.');
      this.statusText = 'Zadané údaje jsou špatné.';
    } else {
      this.serverError();
    }
    this.statusState = -1;
  }


  serverError() {
    this._toastrService.error('Server error!', 'An error occurred on the server.');
    this.statusText = 'An error occurred on the server, please try again later.';
    this.statusState = -1;
  }


  finishSign() {
    setTimeout(() => {
        window.location.replace('/overview');
      },
      1500);
  }


  rememberUser(userLogin, response) {
    if (userLogin.value.remember) {
      this._cookieService.set('refreshToken', response.data.refresh_token);
      this._cookieService.set('validity', response.data.expires_in);
      this._cookieService.set('remember', 'yes');
    } else {
      this._cookieService.delete('refreshToken');
      this._cookieService.delete('validity');
      this._cookieService.delete('remember');
    }
  }
}
