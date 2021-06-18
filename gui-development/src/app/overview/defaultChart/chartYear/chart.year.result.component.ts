import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-year-result',
  templateUrl: './chart.year.result.component.html',
  styleUrls: ['./chart.year.result.component.css']
})
export default class ChartYearResultComponent {

  @Input() size;
  @Input() yearPlan;
  @Input() items;

  getName(name) {
    if (name.length > 16) {
      return name.substring(0, 16) + '...';
    } else {
      return name;
    }
  }

  getBuyingYear(index, item) {
    if (item[index] !== undefined) {
      if (item[index].length !== 0) {
        return item[index].yearIndex + '. rok';
      } else {
        return '>30 let';
      }
    } else {
      return '>30 let';
    }
  }

}
