import {Injectable} from '@angular/core';
import axios from 'axios';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {environment} from '../../environments/environment';


@Injectable()
export default class ChatBotService {

  constructor(private _cookieService: CookieService) {
  }

  sendMessageToBot(dtoIn) {
    return axios({
      method: 'POST',
      url: environment.urlChatBot + '/chatbot/question',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'authorization': 'Bearer ' + this._cookieService.get('accessToken'),
        'X-XSRF-TOKEN': this._cookieService.get('XSRF-TOKEN')
      },
      data: dtoIn
    });
  }
}
