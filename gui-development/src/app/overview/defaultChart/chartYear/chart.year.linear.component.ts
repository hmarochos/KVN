import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {ChartComponent} from 'angular2-chartjs';

@Component({
  selector: 'app-chart-year-linear',
  templateUrl: './chart.year.linear.component.html',
  styleUrls: ['../default.chart.component.css']
})


export default class ChartYearLinearComponent implements OnChanges {

  // Grafy
  type;
  data;
  options;

  display = 'chart';

  @Input() linearYearChartSize;
  @Input() yearPlan;
  @Input() savedMoney;
  @Input() numberOfItems;
  @Output() hideChart = new EventEmitter();
  @Output() funcReturnChartSize = new EventEmitter();

  @ViewChild(ChartComponent) chart: ChartComponent;

  constructor() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    setTimeout(() => {
        if (this.display === 'chart' && this.yearPlan.length !== 0) {
          this.renderChart();
          this.addDefaultChartData();
        }
      },
      1);
  }

  addDefaultChartData() {
    for (let i = 0; i < 6; i++) {
      this.addYear();
    }
  }

  addYear() {
    if (this.data.labels.length < this.yearPlan.annualOverviewMinus5List.length + 1) {
      this.data.labels.push(this.data.labels.length);
      this.data.datasets[0].data.push(this.yearPlan.annualOverviewMinus5List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[1].data.push(this.yearPlan.annualOverviewMinus25List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[2].data.push(this.yearPlan.annualOverview0List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[3].data.push(this.yearPlan.annualOverviewPlus25List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[4].data.push(this.yearPlan.annualOverviewPlus5List[this.data.labels.length - 2].amountSaved);
      this.chart.chart.update();
    }
  }

  removeYear() {
    if (this.data.labels.length > 2) {
      this.data.labels.splice(-1, 1);
      this.chart.chart.update();
    }
  }


  closeChart(name) {
    this.hideChart.emit(name);
  }

  plusSize() {
    this.funcReturnChartSize.emit(this.linearYearChartSize + 1);
  }

  minusSize() {
    this.funcReturnChartSize.emit(this.linearYearChartSize - 1);
  }

  minimize() {
    this.funcReturnChartSize.emit(4);
  }

  maximize() {
    this.funcReturnChartSize.emit(12);
  }

  private renderChart() {
    this.type = 'line';
    this.data = {
      labels: [0],
      datasets: [{
        backgroundColor: 'rgba(165,42,42,0.6)',
        data: [this.savedMoney],
        label: '-5%',
        borderColor: '#a52a2a',
        fill: true
      }, {
        backgroundColor: 'rgba(128, 76, 0, 0.8)',
        data: [this.savedMoney],
        label: '-2.5%',
        borderColor: '#804c00',
        fill: true
      }, {
        backgroundColor: 'rgba(255, 152, 0, 0.8)',
        data: [this.savedMoney],
        label: '0%',
        borderColor: '#ff9800',
        fill: true
      }, {
        backgroundColor: 'rgba(230, 57, 0, 0.8)',
        data: [this.savedMoney],
        label: '2.5%',
        borderColor: '#e63900',
        fill: true
      }, {
        backgroundColor: 'rgba(204, 204, 0, 0.8)',
        data: [this.savedMoney],
        label: '5%',
        borderColor: '#cccc00',
        fill: true
      }]
    };
    this.options = {
      title: {
        display: false,
      },
      scales: {
        yAxes: [{
          scaleLabel: {
            display: true,
            labelString: 'Zůstatek'
          }
        }],
        xAxes: [{
          scaleLabel: {
            display: true,
            labelString: 'Roky'
          }
        }]
      },
      tooltips: {
        mode: 'index',
        intersect: false,
      },
      responsive: true,
      maintainAspectRatio: false
    };

  }
}
