import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-month-result',
  templateUrl: './chart.month.result.component.html',
  styleUrls: ['./chart.month.result.component.css']
})
export default class ChartMonthResultComponent {

  @Input() size;
  @Input() monthPlan;
  @Input() items;

  getName(name) {
    if (name.length > 16) {
      return name.substring(0, 16) + '...';
    } else {
      return name;
    }
  }

  getBuyingMonth(index, item) {
    if (item[index] !== undefined) {
      if (item[index].length !== 0) {
        return item[index].monthIndex + '. m.';
      } else {
        return '>30 m.';
      }
    } else {
      return '>30 m.';
    }
  }

}
