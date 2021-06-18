import {Component, OnInit} from '@angular/core';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {Router} from '@angular/router';


@Component({
  templateUrl: './overview.index.component.html',
  styleUrls: ['./overview.index.component.css'],
  providers: [CookieService]
})



export default class OverviewIndexComponent implements OnInit {
  indexMenu = 1;

  constructor(private _cookieService: CookieService, private _router: Router) {
  }

  ngOnInit() {
    if (!this._cookieService.get('accessToken')) {
      return this._router.navigate(['unautentized']);
    }
  }


}
