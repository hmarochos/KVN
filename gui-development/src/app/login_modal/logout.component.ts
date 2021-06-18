import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CookieService} from 'ngx-cookie-service/dist-lib';


@Component({
  template: ''
})

export default class HomeComponent implements OnInit {

  constructor(private _router: Router, private _routeA: ActivatedRoute, private _cookieService: CookieService) {
  }

  ngOnInit(): void {
    this._routeA.queryParams.subscribe(params => {
      if (params['status'] && this._cookieService.get('accessToken')) {
        this._cookieService.delete('accessToken');
        this._cookieService.delete('refreshToken');
        this._cookieService.delete('validity');
        this._cookieService.delete('remember');
      }
    });
    window.location.assign('/');
  }

}
