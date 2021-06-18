import {Injectable} from '@angular/core';
import axios from 'axios';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {environment} from 'environments/environment';


@Injectable()
export default class UserService {

  userSaveProfile;

  constructor(private _cookieService: CookieService) {
  }


  signInUser(dtoIn) {
    const reqData = {
      grant_type: 'password',
      client_id: environment.client_id,
      client_secret: environment.client_secret,
      username: dtoIn.username,
      password: dtoIn.password
    };

    return axios({
      method: 'post',
      url: environment.url_financial_planning_BE + '/oauth/token',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      data: Object.keys(reqData).map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(reqData[key]);
      }).join('&')
    });
  }


  getUserProfile() {
    return axios({
      method: 'GET',
      url: environment.url_financial_planning_BE + '/user',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
    });
  }


  putUpdateUser(editedUserProfile) {
    this.userSaveProfile = {
      'firstName': editedUserProfile.firstName,
      'lastName': editedUserProfile.lastName,
      'originalEmail': editedUserProfile.email,
      'updatedEmail': editedUserProfile.email,
      'accountId': this.setNullData(editedUserProfile.accountId),
      'telephoneNumber': editedUserProfile.telephoneNumber,
      address: {
        'street': editedUserProfile.address.street,
        'city': editedUserProfile.address.city,
        'psc': editedUserProfile.address.psc
      }
    };

    return axios({
      method: 'PUT',
      url: environment.url_financial_planning_BE + '/user',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
      data: this.userSaveProfile
    });
  }


  deleteUser(email) {
    return axios({
      method: 'DELETE',
      url: environment.url_financial_planning_BE + '/user',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
      data: {email: email}
    });
  }


  changeUserPassword(pwData) {
    return axios({
      method: 'POST',
      url: environment.url_financial_planning_BE + '/user/change-password',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
      data: pwData
    });
  }


  postUserRegistration(userDtoIn) {
    return axios({
      method: 'post',
      url: environment.url_financial_planning_BE + '/user',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
      data: userDtoIn
    });
  }

  setNullData(data) {
    let returnedData;
    if (data === undefined || data === null) {
      returnedData = null;
    } else {
      if (data.toString().replace('\s', '').length === 0) {
        returnedData = null;
      } else {
        returnedData = data;
      }
    }
    return returnedData;
  }
}
