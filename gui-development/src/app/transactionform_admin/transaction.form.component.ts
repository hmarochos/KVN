import {Component, OnInit} from '@angular/core';
import * as moment from 'moment';
import TransactionValidationService from '../frontend_services/transaction.validation.service';
import TransactionService from '../backend_services/transaction.service';
import {ToastrService} from 'ngx-toastr';
import {Router} from '@angular/router';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import UserService from '../backend_services/user.service';
import TokenService from '../backend_services/tokenService';


@Component({
  templateUrl: './transaction.form.component.html',
  providers: [TransactionValidationService, TransactionService, UserService, TokenService],
})


export default class TransactionFormAdminComponent implements OnInit {

  dtoIn;
  currency = 'CZK';
  direction = 'INCOMING';
  transactionType = 'PAYMENT_HOME';
  paymentDate;
  validationInfo = [];
  loading = false;
  toastSetting = {progressBar: true, positionClass: 'toast-top-right', timeOut: 6000};


  constructor(private _transactionValidation: TransactionValidationService, private _transactionService: TransactionService,
              private _toastService: ToastrService, private _router: Router, private _cookieService: CookieService,
              private _userService: UserService, private _tokenService: TokenService) {
  }


  addTransaction(transactionForm) {
    if (this.validationInputs(transactionForm.value).length === 0) {

      const dtoIn = {
        accountId: transactionForm.value.accountId,
        value: {
          amount: transactionForm.value.amount,
          currency: transactionForm.value.currency,
        },
        bankCode: transactionForm.value.bankCode,
        partyDescription: transactionForm.value.partyDescription,
        direction: transactionForm.value.direction,
        transactionType: transactionForm.value.transactionType,
        additionalInfoDomestic: {
          constantSymbol: this._transactionService.setUnusedData(transactionForm.value.constantSymbol),
          variableSymbol: this._transactionService.setUnusedData(transactionForm.value.variableSymbol),
          specificSymbol: this._transactionService.setUnusedData(transactionForm.value.specificSymbol)
        },
        paymentDate: transactionForm.value.paymentDate + 'Z'
      };
      this.loading = true;
      this._transactionService.createTransaction(dtoIn)
        .then(() => {
          this.loading = false;
          this._toastService.success('Transakce vytvořena', 'Hotovo', this.toastSetting);
        }).catch(err => {
        this.loading = false;
        if (err.response !== undefined) {
          if (err.response.data.error === 'invalid_token') {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.addTransaction(transactionForm);
                } else {
                  this.tokenIsInvalid()
                }
              })
              .catch(err => {
                console.log(err);
                this.tokenIsInvalid()
              })
          } else {
            this.serverResponseErrorMessage(err.response.data.message);
          }
        } else {
          this.serverResponseError();
        }
      });
    }
  }

  tokenIsInvalid() {
    this._toastService.error('Pro přístup je nutné se znovu přihlásit.', 'Přihlášení vypršelo:', this.toastSetting);
    this._cookieService.delete('accessToken');
    return this._router.navigate(['/unautentized']);
  }

  serverResponseErrorMessage(message) {
    this._toastService.error(message, 'Error', this.toastSetting);
  }

  serverResponseError() {
    this._toastService.error('An error occurred on the server.', 'Error', this.toastSetting);
  }

  validationInputs(input) {
    this.validationInfo = [];
    let message;
    message = this._transactionValidation.accountIdValidation(input.accountId);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._transactionValidation.validateAmount(input.amount);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._transactionValidation.validateBankCode(input.bankCode);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._transactionValidation.validatePartyDescription(input.partyDescription);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._transactionValidation.validateSymbols(input.constantSymbol);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._transactionValidation.validateSymbols(input.variableSymbol);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    message = this._transactionValidation.validateSymbols(input.specificSymbol);
    if (message !== '') {
      this.validationInfo.push(message);
    }
    return this.validationInfo;
  }


  public setMoment(selectedmoment: any): any {
    this.paymentDate = moment(selectedmoment).format('YYYY-MM-DDTHH:mm');
  }

  ngOnInit() {
    if (!this._cookieService.get('accessToken')) {
      return this._router.navigate(['unautentized']);
    } else {
      this.paymentDate = moment().add(1, 'hours').format('YYYY-MM-DDTHH:mm');
    }
  }


}
