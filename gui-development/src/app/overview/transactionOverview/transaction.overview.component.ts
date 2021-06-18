import {Component} from '@angular/core';
import * as moment from 'moment';
import {ToastrService} from 'ngx-toastr';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {Router} from '@angular/router';
import TransactionService from '../../backend_services/transaction.service';
import TokenService from '../../backend_services/tokenService';

@Component({
  selector: 'app-transaction-overview',
  templateUrl: './transaction.overview.component.html',
  providers: [TransactionService, CookieService, TokenService]
})
export default class TransactionOverviewComponent {

  dateFrom;
  dateTo;
  dtoIn;
  loading = false;
  toastSetting = {progressBar: true, positionClass: 'toast-top-right', timeOut: 6000};
  responseMessage = {status: 0, message: ''};
  transactionItems;
  transactionDateFromText;
  transactionDateToText;
  buttonDisabled = false;

  constructor(private _transactionService: TransactionService, private _toastService: ToastrService,
              private _cookieService: CookieService, private _router: Router, private _tokenService: TokenService) {
    this.dateFrom = moment().subtract(1, 'months').startOf('month').format('YYYY-MM-DD HH:mm');
    this.dateTo = moment().subtract(1, 'months').endOf('month').format('YYYY-MM-DD HH:mm');
  }


  public setDateFrom(selectedmoment: any): any {
    if (moment(this.dateTo) > moment(selectedmoment)) {
      this.dateFrom = moment(selectedmoment).format('YYYY-MM-DD HH:mm');
    } else {
      this._toastService.error('Datum od nemůže být starší, než datum do.', 'Error');
    }
  }


  public setDateTo(selectedmoment: any): any {
    if (moment(this.dateFrom) < moment(selectedmoment)) {
      this.dateTo = moment(selectedmoment).format('YYYY-MM-DD HH:mm');
    } else {
      this._toastService.error('Datum do nemůže být dřívěnjší, než datum od.', 'Error');
    }
  }


  showTransactionClicked() {
    this.responseMessage = {status: 0, message: ''};
    const dtoIn = {
      dateFrom: this.dateFrom.replace(' ', 'T') + 'Z',
      dateTo: this.dateTo.replace(' ', 'T') + 'Z'
    };
    this.loading = true;
    this._transactionService.getTransaction(dtoIn)
      .then(resp => {
        this.transactionItems = [];
        this.loading = false;
        this.serverItemsResponseOK(resp);
        this.disableButton();
      }).catch(err => {
      this.disableButton();
      this.loading = false;
      if (err.response !== undefined) {
        if (err.response.data.error === 'invalid_token') {
          this._tokenService.handleRefreshToken()
            .then(resp => {
              if (resp) {
                this.showTransactionClicked();
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


  disableButton() {
    this.buttonDisabled = true;
    setTimeout(() => {
        this.buttonDisabled = false;
      },
      2000);
  }


  serverItemsResponseOK(resp) {
    this.transactionItems = resp.data.body.bankTransactionDtoOutList;
    this.transactionDateFromText = this.dateFrom;
    this.transactionDateToText = this.dateTo;
  }


  tokenIsInvalid() {
    this._toastService.error('Pro přístup je nutné se znovu přihlásit.', 'Přihlášení vypršelo:', this.toastSetting);
    this._cookieService.delete('accessToken');
    return this._router.navigate(['/unautentized']);
  }


  serverResponseErrorMessage(message) {
    this._toastService.error(message, 'Error', this.toastSetting);
    this.responseMessage = {status: -1, message: message};
  }


  serverResponseError() {
    this._toastService.error('An error occurred on the server.', 'Error', this.toastSetting);
    this.responseMessage = {status: -1, message: 'An error occurred on the server, opakujte akci později.'};
  }

  translate(itemName) {
    switch (itemName) {
      case 'PAYMENT_HOME':
        return 'Domácí';
      case 'PAYMENT_ABROAD':
        return 'Zahraniční';
      case 'SAVING':
        return 'Spoření';
      default:
        return itemName;
    }
  }
}
