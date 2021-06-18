import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';

import {AppComponent} from './app.component';
import {RouterModule, Routes} from '@angular/router';
import HomeComponent from './home/home.component';
import {ModalComponent} from './login_modal/modal.component';
import {ToastrModule} from 'ngx-toastr';
import PageNotFoundComponent from './not_found_component/page.not.found.component';
import LogoutComponent from './login_modal/logout.component';
import ProfileComponent from './profile/profile.component';
import UnautentizatedUserComponent from './unautentizated_user/unautentizated_user.component';
import SpinnerComponent from './project_tools/spinner/spinner.component';
import {LoadersCssModule} from 'angular2-loaders-css';
import PasswordChangeComponent from './profile/password_change/passwordchange.component';
import AboutComponent from './about/about.component';
import RegisterComponent from './register/register.component';
import OverviewIndexComponent from './overview/overview.index.component';
import DefaultChartComponent from './overview/defaultChart/default.chart.component';
import {DragulaModule} from 'ng2-dragula';
import {ChartModule} from 'angular2-chartjs';
import ChartMonthBoxComponent from './overview/defaultChart/chartMonth/chart.month.box.component';
import ChartYearLinearComponent from './overview/defaultChart/chartYear/chart.year.linear.component';
import ContactComponent from './contact/contact.component';
import ChatBotComponent from './chatBot/chatbot.component';
import ChartMonthBydaysComponent from './overview/defaultChart/chartMonthByDays/chart.month.bydays.component';
import ChartMonthLinearComponent from './overview/defaultChart/chartMonth/chart.month.linear.component';
import ChartYearBoxComponent from './overview/defaultChart/chartYear/chart.year.box.component';
import {TooltipModule} from 'ng2-tooltip-directive';
import ItemEditComponent from './overview/defaultChart/itemEdit/item.edit.component';
import TransactionFormAdminComponent from './transactionform_admin/transaction.form.component';
import {DateTimePickerModule} from 'ng-pick-datetime';
import TransactionOverviewComponent from './overview/transactionOverview/transaction.overview.component';
import ChartMonthResultComponent from './overview/defaultChart/chartMonth/chart.month.result.component';
import ChartYearResultComponent from './overview/defaultChart/chartYear/chart.year.result.component';


const appRoutes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'logout', component: LogoutComponent},
  {path: 'profile', component: ProfileComponent},
  {path: 'unautentized', component: UnautentizatedUserComponent},
  {path: 'about', component: AboutComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'overview', component: OverviewIndexComponent},
  {path: 'contact', component: ContactComponent},
  {path: 'create-transaction', component: TransactionFormAdminComponent},
  {path: '**', component: PageNotFoundComponent}

];


const ToastSetting = {progressBar: true, positionClass: 'toast-top-right', timeOut: 2000};


@NgModule({
  declarations: [
    AppComponent, HomeComponent, ModalComponent, PageNotFoundComponent, LogoutComponent, ProfileComponent, UnautentizatedUserComponent
    , SpinnerComponent, PasswordChangeComponent, AboutComponent, RegisterComponent, OverviewIndexComponent, DefaultChartComponent,
    ChartMonthBoxComponent, ChartMonthLinearComponent, ChartYearBoxComponent, ChartYearLinearComponent, ContactComponent,
    ChatBotComponent, ChartMonthBydaysComponent, ItemEditComponent, TransactionFormAdminComponent, TransactionOverviewComponent,
    ChartMonthResultComponent, ChartYearResultComponent],
  imports: [TooltipModule, DragulaModule, ChartModule, LoadersCssModule, ToastrModule.forRoot(ToastSetting), BrowserModule,
    FormsModule, HttpModule, RouterModule, RouterModule.forRoot(appRoutes), DateTimePickerModule],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
