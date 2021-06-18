import {Injectable} from '@angular/core';
import WishListService from '../../backend_services/wishlist.service';

@Injectable()
export default class DefaultChartService {

  constructor(private _wishListService: WishListService) {
  }

  getMonthOverview() {
    return this._wishListService.getMountTransOverview();
  }

  getMonthPlan(dtoIn) {
    return this._wishListService.getMonthPlan(dtoIn);
  }

  getYearPlan(dtoIn) {
    return this._wishListService.getYearPlan(dtoIn);
  }

}
