import {Injectable} from '@angular/core';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {environment} from '../../environments/environment';
import axios from 'axios';

@Injectable()
export default class TokenService {

  constructor(private _cookieService: CookieService) {
  }

  rememberMe() {
    return this._cookieService.get('remember') === 'yes'
  }

  setCookiesData(response) {
    this._cookieService.set('accessToken', response.data.access_token);
    this._cookieService.set('refreshToken', response.data.refresh_token);
    this._cookieService.set('validity', response.data.expires_in);
  }

  handleRefreshToken() {
    return new Promise((resolve, reject) => {
      if (this.rememberMe()) {
        this.refreshToken()
          .then(resp => {
            this.setCookiesData(resp);
            resolve(true);
          }).catch(err => {
          console.log(err);
          reject(err);
        });
      } else {
        resolve(false);
      }
    });
  }

  refreshToken() {
    const dtoIn = {
      grant_type: 'refresh_token',
      client_id: environment.client_id,
      client_secret: environment.client_secret,
      refresh_token: this._cookieService.get('refreshToken'),
    };

    return axios({
      method: 'post',
      url: environment.url_financial_planning_BE + '/oauth/token',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      data: Object.keys(dtoIn).map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(dtoIn[key]);
      }).join('&')
    });
  }

}
