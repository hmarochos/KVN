import {Component, Input, OnInit} from '@angular/core';
import DefaultChartService from './default.chart.service';
import {DragulaService} from 'ng2-dragula';
import WishItemValidationService from '../../frontend_services/wishItem.validation.service';
import WishListService from '../../backend_services/wishlist.service';
import {ToastrService} from 'ngx-toastr';
import {CookieService} from 'ngx-cookie-service/dist-lib';
import {Router} from '@angular/router';
import TokenService from '../../backend_services/tokenService';


@Component({
  selector: 'app-chart',
  templateUrl: './default.chart.component.html',
  styleUrls: ['./default.chart.component.css'],
  providers: [DefaultChartService, WishItemValidationService, WishListService, ToastrService,
    CookieService, DefaultChartService, TokenService]
})

export default class DefaultChartComponent implements OnInit {
  // Itemy ve wishlistu
  items: any [];
  filteredItems: any [];
  defaultFilteredItems: any [];

  [key: string]: any;

  // Pro filtrování obsahu
  @Input() menuIndex: number;

  // Defaultní nastavení
  savedMoney = 0;
  defaultSavedMoney;
  clearProfit;
  monthProfit;
  profitDiff;

  // Input pro přidání itemu
  wishItemPrice = '';
  wishItemName = '';
  wishItemDescription = '';
  wishItemValidationInfo = [];
  wishItem;
  listInfo;

  // Zobrazování obsahu
  showHideChartByDays = false;
  displayChart = {monthLinear: true, monthBox: false, yearLinear: false, yearBox: false};

  // Pagination
  totalPages;
  currentPage;
  lastPage;
  firstPage = 1;

  // Grafy
  linearMonthChartSize = 6;
  boxMonthChartSize = 6;
  linearYearChartSize = 6;
  boxYearChartSize = 6;
  monthPlan = [];
  yearPlan = [];
  monthDataOverview;

  responseMessage = {status: 0, message: ''};
  loading = false;
  toastSetting = {progressBar: true, positionClass: 'toast-top-right', timeOut: 6000};

  // Detail, Editace - pro editaci itemu či pouhé zobrazení
  webContext = '';
  selectedItemToEdit;


  constructor(private _dragula: DragulaService, private _wishItemValidation: WishItemValidationService,
              private _wishListService: WishListService, private _toastService: ToastrService, private _cookieService: CookieService,
              private _router: Router, private _defaultChartService: DefaultChartService, private _tokenService: TokenService) {
  }

  // nastavení velikosti grafů START
  getLinearMonthChangeSize(childData) {
    if (childData < 13 && childData > 3) {
      this.linearMonthChartSize = childData;
    }
  }

  getBoxMonthChangeSize(childData) {
    if (childData < 13 && childData > 3) {
      this.boxMonthChartSize = childData;
    }
  }

  getLinearYearChangeSize(childData) {
    if (childData < 13 && childData > 3) {
      this.linearYearChartSize = childData;
    }
  }

  getBoxYearChangeSize(childData) {
    if (childData < 13 && childData > 3) {
      this.boxYearChartSize = childData;
    }
  }

  // nastavení velikosti grafů KONEC

  ngOnInit(): void {

    this.items = [];
    this.getWishList();

    this.initialization();

    this._dragula
      .drop
      .subscribe(drag => {
        if (drag[0] === 'bag-one') {
          const changePriorityWishInfoList = [];
          for (let i = 0; i < this.filteredItems.length; i++) {
            if (this.filteredItems[i].id !== this.defaultFilteredItems[i].id) {
              changePriorityWishInfoList.push({
                id: this.filteredItems[i].id,
                name: this.filteredItems[i].name,
                priority: ((this.currentPage - 1) * 9 + i + 1)
              });
            }
          }
          const changeData = {changePriorityWishInfoList: changePriorityWishInfoList};
          this.putChangePriority(changeData);
        }
      });
  }

  // Změna priority START
  putChangePriority(changeData) {
    this.loading = true;
    this._wishListService.putPriorityChange(changeData)
      .then(() => {
        this.loading = false;
        this.reOrganizePriorityFE(changeData.changePriorityWishInfoList);
        this.priorityChangeResponseOk();
      }).catch(err => {
      this.loading = false;
      this.initialization();
      if (err.response !== undefined) {
        if (err.response.data.error === 'invalid_token') {
          this._tokenService.handleRefreshToken()
            .then(resp => {
              if (resp) {
                this.putChangePriority(changeData);
              } else {
                this.tokenIsInvalid()
              }
            })
            .catch(err => {
              console.log(err);
              this.tokenIsInvalid()
            })
        } else {
          this.getWishList();
        }
      } else {
        this.serverGetResponseError();
      }
    });
  }

  reOrganizePriorityFE(changedItems) {
    for (let i = 0; i < changedItems.length; i++) {
      this.items.filter(item => {
        if (item.id === changedItems[i].id) {
          item.priority = changedItems[i].priority;
        }
      });
    }
    this.items.sort(function (a, b) {
      const keyA = a.priority;
      const keyB = b.priority;

      if (keyA < keyB) {
        return -1;
      }
      if (keyA > keyB) {
        return 1;
      }
      return 0;
    });
  }


  priorityChangeResponseOk() {
    this.defaultFilteredItems = [];
    for (let i = 0; i < this.filteredItems.length; i++) {
      this.defaultFilteredItems.push(this.filteredItems[i]);
    }
    this.getPlan();
  }

  // Změna priority KONEC

  changeProfit() {
    return this.clearProfit = this.monthProfit + this.profitDiff;
  }


  // GET list START
  getWishList() {
    this.listInfo = {
      'page': 0,
      'size': 1000,
      'order': 'ASC',
      'orderBy': 'priority'
    };
    this.loading = true;
    this._wishListService.getWishList(this.listInfo)
      .then(resp => {
        this.loading = false;
        this.getListResponseOk(resp.data.body.wishList);
      }).catch(err => {
      this.loading = false;
      if (err.response !== undefined) {
        if (err.response.data.error === 'invalid_token') {
          this._tokenService.handleRefreshToken()
            .then(resp => {
              if (resp) {
                this.getWishList();
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
        this.serverGetResponseError();
      }
    });
  }


  getListResponseOk(wishItems) {
    this.items = [];
    for (let i = 0; i < wishItems.length; i++) {
      this.items.push(wishItems[i]);
    }
    this.getMonthOverview();
    this.initialization();
  }


  serverGetResponseError() {
    this._toastService.error('An error occurred on the server.', 'Error', this.toastSetting);
    this.responseMessage = {status: -2, message: 'Omlouváme se, na serveru došlo k chybě.'};
  }

  // GET list KONEC

  // GET měsíční přehled START

  getMonthOverview() {
    this.loading = true;
    this._defaultChartService.getMonthOverview()
      .then(resp => {
        this.loading = false;
        this.getDailyTransactionResponseOk(resp.data.body.dailyTransactionsResultList);
      }).catch(err => {
      this.loading = false;
      if (err.response !== undefined) {
        if (err.response.data.error === 'invalid_token') {
          this._tokenService.handleRefreshToken()
            .then(resp => {
              if (resp) {
                this.getMonthOverview();
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
        this.serverGetResponseError();
      }
    });
  }

  getDailyTransactionResponseOk(respData) {
    this.monthDataOverview = respData;
    if (!(this.clearProfit > 0)) {
      this.monthProfit = respData[respData.length - 1].result;
      this.clearProfit = this.monthProfit;
      this.profitDiff = '';
    }
  }

  // GET měsíční přehled KONEC

  // GET plán START

  getPlan() {
    if (this.clearProfit > 0) {
      const dtoIn = {
        amountSaved: this.savedMoney,
        monthlyProfit: this.clearProfit
      };

      this._defaultChartService.getMonthPlan(dtoIn)
        .then(resp => {
          this.monthPlan = resp.data.body;
          this.defaultSavedMoney = this.savedMoney;
          this.getYearPlan();
        }).catch(err => {
        this.loading = false;
        if (err.response !== undefined) {
          if (err.response.data.error === 'invalid_token') {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.getPlan();
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
    } else {
      this.monthPlan = [];
      this._toastService.info('To get results, your profit must be positive and non-zero.', 'Monthly profit', this.toastSetting);
    }
  }

  getYearPlan() {
    if (this.clearProfit > 0) {
      const dtoIn = {
        amountSaved: this.savedMoney,
        monthlyProfit: this.clearProfit
      };

      this._defaultChartService.getYearPlan(dtoIn)
        .then(resp => {
          this.yearPlan = resp.data.body;
          this.defaultSavedMoney = this.savedMoney;
        }).catch(err => {
        this.loading = false;
        if (err.response !== undefined) {
          if (err.response.data.error === 'invalid_token') {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.getYearPlan();
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
    } else {
      this.monthPlan = [];
      this._toastService.info('To get results, your profit must be positive and non-zero.', 'Monthly profit', this.toastSetting);
    }
  }

  // GET plán KONEC


  initialization() {
    this.setPagination(this.items.length);
  }

  // Přidání itemu START
  addItem(itemForm) {
    if (this.validateItem().length === 0) {
      this.wishItem = {
        price: this.wishItemPrice,
        currency: 'CZK',
        name: this.wishItemName,
        description: this.wishItemDescription
      };

      itemForm.resetForm();
      this.wishItemName = '';
      this.wishItemDescription = '';
      this.wishItemPrice = '';

      this.loading = true;
      this._wishListService.createWish(this.wishItem)
        .then(response => {
          this.loading = false;
          this.createItemResponseOk(response.data.body);
        }).catch(err => {
        this.loading = false;
        if (err.response !== undefined) {
          if (err.response.data.error === 'invalid_token') {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.addItem(itemForm);
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
  }


  validateItem() {
    this.wishItemValidationInfo = [];
    let message;

    message = this._wishItemValidation.itemNameValidation(this.wishItemName);
    if (message !== '') {
      this.wishItemValidationInfo.push(message);
    }
    message = this._wishItemValidation.itemPriceValidation(this.wishItemPrice);
    if (message !== '') {
      this.wishItemValidationInfo.push(message);
    }
    message = this._wishItemValidation.itemDescriptionValidation(this.wishItemDescription);
    if (message !== '') {
      this.wishItemValidationInfo.push(message);
    }
    return this.wishItemValidationInfo;
  }


  createItemResponseOk(respItem) {
    this._toastService.success('Wishes successfully inserted.', 'Wishes entered', this.toastSetting);
    this.responseMessage = {status: 1, message: 'Wishes successfully inserted.'};
    this.items.push(respItem);
    this.getPlan();
    this.initialization();
  }

  serverResponseErrorMessage(message) {
    this._toastService.error(message, 'Error', this.toastSetting);
    this.responseMessage = {status: -1, message: message};
  }

  serverResponseError() {
    this._toastService.error('An error occurred on the server.', 'Error', this.toastSetting);
    this.responseMessage = {status: -1, message: 'An error occurred on the server.'};
  }

  tokenIsInvalid() {
    this._toastService.error('Pro přístup je nutné se znovu přihlásit.', 'Přihlášení vypršelo:', this.toastSetting);
    this._cookieService.delete('accessToken');
    return this._router.navigate(['/unautentized']);
  }

  // Přidání itemu KONEC

  // Delete item START
  deleteItem(itemId) {
    this.loading = true;
    this._wishListService.deleteItem(itemId)
      .then(() => {
        this.loading = false;
        this.deleteItemResponseOk(itemId);
      }).catch(err => {
      this.loading = false;
      if (err.response !== undefined) {
        if (err.response.data.error === 'invalid_token') {
          this._tokenService.handleRefreshToken()
            .then(resp => {
              if (resp) {
                this.deleteItem(itemId);
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


  deleteItemResponseOk(itemId) {
    this.items = this.items.filter(item => item.id !== itemId);
    this._toastService.success('Přání odstraněno.', 'Odstraněno');
    this.responseMessage = {status: 1, message: 'Přání odstraněno'};
    this.getPlan();
    this.initialization();
  }

  // Delete item KONEC

  // Delete all START
  deleteAllItems() {
    if (confirm('Opravdu chcete Delete allchna přání?')) {
      this.loading = true;
      this._wishListService.deleteAllItems()
        .then(() => {
          this.loading = false;
          this.items = [];
          this.getPlan();
          this.initialization();
        }).catch(err => {
        this.loading = false;
        if (err.response !== undefined) {
          if (err.response.data.error === 'invalid_token') {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.deleteAllItems();
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
  }

  // Delete all END

  // EditItem START
  showDetail(item) {
    this.webContext = 'Detail';
    this.selectedItemToEdit = item;
  }

  showEdit(item) {
    this.webContext = 'Editace';
    this.selectedItemToEdit = item;
  }

  returnToList() {
    this.webContext = '';
  }

  setLoading() {
    this.loading = !this.loading;
  }

  // EditItem KONEC

  // Stránkování wishListu START
  setPagination(itemLength) {
    this.totalPages = Math.ceil(itemLength / 9);
    if (this.totalPages === 0) {
      this.lastPage = 1;
    } else {
      this.lastPage = this.totalPages;
    }
    this.currentPage = 1;
    this.setContentByPage();
  }


  setNextPage() {
    if (this.currentPage !== this.lastPage) {
      this.currentPage = this.currentPage + 1;
      this.setContentByPage();
    }
  }


  setPrevPage() {
    if (this.currentPage !== this.firstPage) {
      this.currentPage = this.currentPage - 1;
      this.setContentByPage();
    }
  }


  setFirstPage() {
    this.currentPage = 1;
    this.setContentByPage();
  }


  setLastPage() {
    this.currentPage = this.lastPage;
    this.setContentByPage();
  }


  setContentByPage() {
    let i = (this.currentPage - 1) * 9;
    this.filteredItems = [];
    this.defaultFilteredItems = [];

    while (this.filteredItems.length !== 9 && this.items.length >= i) {
      if (this.items[i]) {
        this.filteredItems.push(this.items[i]);
        this.defaultFilteredItems.push(this.items[i]);
      }
      i++;
    }
  }

  // Stránkování wishListu KONEC

  showHideChart(name) {
    this.displayChart[name] = !this.displayChart[name];
  }

}
