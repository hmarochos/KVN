import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild} from '@angular/core';
import {ChartComponent} from 'angular2-chartjs';

@Component({
  selector: 'app-chart-month-box',
  templateUrl: './chart.month.box.component.html',
  styleUrls: ['../default.chart.component.css']
})

export default class ChartMonthBoxComponent implements OnChanges {

  // Grafy
  type;
  data;
  options;

  display = 'chart';


  @Input() boxMonthChartSize;
  @Input() monthPlan;
  @Input() savedMoney;
  @Input() numberOfItems;
  @Output() hideChart = new EventEmitter();
  @Output() funcReturnChartSize = new EventEmitter();

  @ViewChild(ChartComponent) chart: ChartComponent;


  constructor() {
  }


  ngOnChanges(changes: SimpleChanges): void {
    setTimeout(() => {
        if (this.display === 'chart' && this.monthPlan.length !== 0) {
          this.renderChart();
          this.addDefaultChartData();
        }
      },
      1);
  }

  addDefaultChartData() {
    for (let i = 0; i < 6; i++) {
      this.addMonth();
    }
  }

  addMonth() {
    if (this.data.labels.length < this.monthPlan.monthlyOverviewMinus5List.length + 1) {
      this.data.labels.push(this.data.labels.length);
      this.data.datasets[0].data.push(this.monthPlan.monthlyOverviewMinus5List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[1].data.push(this.monthPlan.monthlyOverviewMinus25List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[2].data.push(this.monthPlan.monthlyOverview0List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[3].data.push(this.monthPlan.monthlyOverviewPlus25List[this.data.labels.length - 2].amountSaved);
      this.data.datasets[4].data.push(this.monthPlan.monthlyOverviewPlus5List[this.data.labels.length - 2].amountSaved);
      this.chart.chart.update();
    }
  }

  removeMonth() {
    if (this.data.labels.length > 2) {
      this.data.labels.splice(-1, 1);
      this.chart.chart.update();
    }
  }

  closeChart(name) {
    this.hideChart.emit(name);
  }

  plusSize() {
    this.funcReturnChartSize.emit(this.boxMonthChartSize + 1);
  }

  minusSize() {
    this.funcReturnChartSize.emit(this.boxMonthChartSize - 1);
  }

  minimize() {
    this.funcReturnChartSize.emit(4);
  }

  maximize() {
    this.funcReturnChartSize.emit(12);
  }

  renderChart() {
    this.type = 'bar';
    this.data = {
      labels: [0],
      datasets: [{
        id: 1,
        backgroundColor: 'rgba(244, 67, 54,0.8)',
        data: [this.savedMoney],
        label: '-5%',
      }, {
        id: 2,
        backgroundColor: 'rgba(0, 188, 212, 0.8)',
        data: [this.savedMoney],
        label: '-2,5',
      }, {
        id: 3,
        backgroundColor: 'rgba(76, 175, 80, 0.8)',
        data: [this.savedMoney],
        label: '0%',
      }, {
        id: 4,
        backgroundColor: 'rgba(26, 117, 255, 0.8)',
        data: [this.savedMoney],
        label: '2.5%',
        fill: true
      }, {
        id: 5,
        backgroundColor: 'rgba(0, 128, 0, 0.8)',
        data: [this.savedMoney],
        label: '5%',
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
          },
          ticks: {
            beginAtZero: true,
          }
        }],
        xAxes: [{
          scaleLabel: {
            display: true,
            labelString: 'Měsíce'
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

