import {Component, EventEmitter, Input, Output} from '@angular/core';
import UserValidationService from '../../frontend_services/user.validation.service';

@Component({
  selector: 'app-password-change-child',
  templateUrl: './passwordchange.component.html',
  providers: [UserValidationService]
})

export default class PasswordChangeComponent {

  passObj;
  originalPassword;
  newPassword;
  confirmationPassword;
  passwordInfo = [];
  @Input() email;
  @Output() funcReturnChildData = new EventEmitter();

  constructor(private _userValidationService: UserValidationService) {
  }

  changePwClicked(passwordChange) {
    if (this.inputValidation(passwordChange.value).length === 0) {
      this.passObj = {
        email: this.email,
        originalPassword: this.originalPassword,
        newPassword: this.newPassword,
        confirmationPassword: this.confirmationPassword
      };

      this.funcReturnChildData.emit(this.passObj);
    }
  }


  inputValidation(passwordChange) {
    let message;
    this.passwordInfo = [];
    message = this._userValidationService.passwordValidation(passwordChange.originalPassword);
    if (message !== '') {
      this.passwordInfo.push(message);
    }
    message = this._userValidationService.passwordValidation(passwordChange.newPassword);
    if (message !== '' && this.passwordInfo[this.passwordInfo.length - 1] !== message) {
      this.passwordInfo.push(message);
    }
    message = this._userValidationService.passwordValidation(passwordChange.confirmationPassword);
    if (message !== '' && this.passwordInfo[this.passwordInfo.length - 1] !== message) {
      this.passwordInfo.push(message);
    }
    if (passwordChange.newPassword !== passwordChange.confirmationPassword) {
      this.passwordInfo.push('Nové hesla se neshodují.')
    }
    return this.passwordInfo;
  }


}
