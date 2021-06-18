import {Component} from '@angular/core';
import {CookieService} from 'ngx-cookie-service/dist-lib';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [CookieService]
})
export class AppComponent {


  constructor(private _cookiesService: CookieService) {
  }

  getFullYear() {
    return new Date().getFullYear();
  }

}
