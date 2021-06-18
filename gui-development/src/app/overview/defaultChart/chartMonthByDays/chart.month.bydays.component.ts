import {Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild} from '@angular/core';


@Component({
  selector: 'app-chart-month-bydays',
  templateUrl: './chart.month.bydays.component.html'
})

export default class ChartMonthBydaysComponent implements OnChanges {

  // Grafy
  type;
  data;
  options;

  @Input() monthDataOverview;


  @ViewChild('donut') donut: ElementRef;

  constructor() {
  }


  ngOnChanges(changes: SimpleChanges): void {
    this.createChart(this.monthDataOverview);
  }

  createChart(chartData) {
    setTimeout(() => {
        this.renderChart();
        for (let i = 0; i < chartData.length; i++) {
          this.data.labels.push(chartData[i].dayIndex);
          this.data.datasets.find(x => x.id === 1).data.push(chartData[i].result);
          this.data.datasets.find(x => x.id === 1).pointBackgroundColor.push(this.setColor(chartData, i));
        }
      },
      1);
  }

  setColor(chartData, i) {
    if (chartData[i].result >= 0) {
      return 'green';
    } else {
      return 'red';
    }
  }


  renderChart() {
    this.type = 'line';
    this.data = {
      labels: [0],
      datasets: [{
        id: 1,
        data: [0],
        borderColor: 'rgba(31, 113, 255,0.8)',
        backgroundColor: 'rgba(31, 113, 255,0.1)',
        pointBorderColor: 'black',
        pointBackgroundColor: ['green'],
        label: false,
        fill: true
      }]
    };
    this.options = {
      title: {
        display: true,
        text: 'Mounthly movement'
      },
      legend: {
        display: false
      },
      responsive: true,
      maintainAspectRatio: false
    };

  }
}
