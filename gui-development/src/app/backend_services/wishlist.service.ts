import {Injectable} from '@angular/core';
import axios from 'axios';
import {environment} from '../../environments/environment';
import {CookieService} from 'ngx-cookie-service/dist-lib';

@Injectable()
export default class WishListService {

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

  createWish(itemDtoIn) {
    const specifiedUrl = '/wish';
    return this.serverCall('Post', itemDtoIn, specifiedUrl);
  }

  getWishList(listInfo) {
    const specifiedUrl = '/wish/list';
    return this.serverCall('post', listInfo, specifiedUrl);
  }

  deleteItem(itemId) {
    const itemDel = {id: itemId};
    const specifiedUrl = '/wish';
    return this.serverCall('delete', itemDel, specifiedUrl);
  }

  deleteAllItems() {
    const specifiedUrl = '/wish/delete-all';
    return this.serverCall('delete', '', specifiedUrl);
  }

  editItem(editedItem) {
    const specifiedUrl = '/wish';
    return this.serverCall('put', editedItem, specifiedUrl);
  }

  getMountTransOverview() {
    const specifiedUrl = '/transaction/last-month-transactions-overview';
    return this.serverCall('get', '', specifiedUrl);
  }

  putPriorityChange(dtoIn) {
    const specifiedUrl = '/wish/change-priority';
    return this.serverCall('put', dtoIn, specifiedUrl);
  }

  getMonthPlan(dtoIn) {
    const specifiedUrl = '/financial-planning/monthly-plan';
    return this.serverCall('post', dtoIn, specifiedUrl);
  }

  getYearPlan(dtoIn) {
    const specifiedUrl = '/financial-planning/annual-plan';
    return this.serverCall('post', dtoIn, specifiedUrl);
  }
}
