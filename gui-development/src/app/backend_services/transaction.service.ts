import {Injectable} from '@angular/core';
import axios from 'axios';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {environment} from '../../environments/environment';


@Injectable()
export default class TransactionService {

  constructor(private _cookieService: CookieService) {
  }

  serverCall(method, dtoIn, specifiedUrl) {
    return axios({
      method: method,
      url: environment.url_financial_planning_BE + specifiedUrl,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
      data: dtoIn,
    });
  }

  createTransaction(itemDtoIn) {
    const specifiedUrl = '/transaction';
    return this.serverCall('Post', itemDtoIn, specifiedUrl);
  }

  getTransaction(itemDtoIn) {
    const specifiedUrl = '/transaction/get-by-date-interval';
    return this.serverCall('Post', itemDtoIn, specifiedUrl);
  }

  setUnusedData(data) {
    let returnedData;
    if (data === undefined || data === null) {
      returnedData = '';
    } else {
      if (data.toString().replace('\s', '').length === 0) {
        returnedData = '';
      } else {
        returnedData = data;
      }
    }
    return returnedData;
  }

}
