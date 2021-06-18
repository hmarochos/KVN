import {Component, EventEmitter, OnChanges, Output, SimpleChanges} from '@angular/core';
import WishItemValidationService from '../../../frontend_services/wishItem.validation.service';
import {Input} from '@angular/core/src/metadata/directives';
import WishListService from '../../../backend_services/wishlist.service';
import {ToastrService} from 'ngx-toastr';
import TokenService from '../../../backend_services/tokenService';

@Component({
  selector: 'app-show-edit-item',
  templateUrl: './item.edit.component.html',
  providers: [WishItemValidationService, WishListService, ToastrService, TokenService, TokenService]
})


export default class ItemEditComponent implements OnChanges {

  @Input() oldItem;
  @Input() viewType;
  @Output() tokenIsInvalid: EventEmitter<any> = new EventEmitter();
  @Output() serverResponseErrorMessage: EventEmitter<any> = new EventEmitter();
  @Output() serverResponseError: EventEmitter<any> = new EventEmitter();
  @Output() getWishList: EventEmitter<any> = new EventEmitter();
  @Output() returnToList: EventEmitter<any> = new EventEmitter();
  @Output() setLoading: EventEmitter<any> = new EventEmitter();
  @Output() getPlan: EventEmitter<any> = new EventEmitter();

  newItem = {name: '', price: '', description: ''};
  wishItemValidationInfo;

  constructor(private _wishItemValidation: WishItemValidationService, private _wishListService: WishListService,
              private _toastService: ToastrService, private _tokenService: TokenService) {
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (this.oldItem !== undefined) {
      this.newItem = this.oldItem;
    }
  }


  editItem() {
    if (this.validateItem().length === 0) {
      this.setLoading.emit();
      this.wishItemValidationInfo = '';
      this._wishListService.editItem(this.newItem)
        .then(() => {
          this.setLoading.emit();
          this.editItemResponseOK();
        }).catch(err => {
        this.setLoading.emit();
        if (err.response !== undefined) {
          if (err.response.data.error === 'invalid_token') {
            this._tokenService.handleRefreshToken()
              .then(resp => {
                if (resp) {
                  this.editItem();
                } else {
                  this.tokenIsInvalid.emit();
                }
              })
              .catch(err => {
                console.log(err);
                this.tokenIsInvalid.emit();
              })
          } else {
            this.serverResponseErrorMessage.emit(err.response.data.message);
            this.wishItemValidationInfo = err.response.data.message;
          }
        } else {
          this.serverResponseError.emit();
        }
      });
    }
  }


  validateItem() {
    this.wishItemValidationInfo = [];
    let message;

    message = this._wishItemValidation.itemNameValidation(this.newItem.name);
    if (message !== '') {
      this.wishItemValidationInfo.push(message);
    }
    message = this._wishItemValidation.itemPriceValidation(this.newItem.price);
    if (message !== '') {
      this.wishItemValidationInfo.push(message);
    }
    message = this._wishItemValidation.itemDescriptionValidation(this.newItem.description);
    if (message !== '') {
      this.wishItemValidationInfo.push(message);
    }
    return this.wishItemValidationInfo;
  }


  editItemResponseOK() {
    this.getWishList.emit();
    this._toastService.success('Přání bylo úspěšně změněno.', 'Přání změněno.');
    this.returnToList.emit();
    this.getPlan.emit();
    this.newItem = {name: '', price: '', description: ''};
  }

}
