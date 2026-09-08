import { Component, Input, OnChanges } from '@angular/core';
import { ApexDataLabels, ApexGrid, ApexMarkers, NgApexchartsModule } from 'ng-apexcharts';
import { DashboardGrowth } from '../../../core/models/super-admin-dashboard.model';
import { ApexAxisChartSeries, ApexChart, ApexLegend, ApexStroke, ApexTooltip, ApexXAxis, ApexYAxis } from 'ng-apexcharts';
import { __values } from 'tslib';
import { timestamp } from 'rxjs';
import { GrowthSeries } from '../../../core/models/growth-series.model';

export type GrowthChartOptions = {
  series: ApexAxisChartSeries,
  chart: ApexChart,
  xaxis: ApexXAxis,
  yaxis: ApexYAxis | ApexYAxis[],
  stroke: ApexStroke,
  legend: ApexLegend,
  tooltip: ApexTooltip,
  markers: ApexMarkers,
  grid: ApexGrid,
  dataLabels: ApexDataLabels,
  colors: string[]
}

@Component({
  selector: 'app-growth-chart',
  imports: [NgApexchartsModule],
  templateUrl: './growth-chart.component.html',
  styleUrl: './growth-chart.component.scss'
})
export class GrowthChartComponent implements OnChanges {
  @Input({ required: true })
  growth!: {
    years: number[],
    months: number[]
  };

  @Input({ required: true })
  series!: GrowthSeries[];

  @Input({ required: true })
  period!: string;

  chartOptions!: Partial<GrowthChartOptions>;

  ngOnChanges() {
    if (!this.growth) return;
    this.initializeCharts();
  }

  private readonly MONTHS = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec'
  ];

  private buildLabels(): string[] {
    return this.growth.months.map((month, index) => `${this.MONTHS[month - 1]} ${this.growth.years[index]}`);
  }

  private initializeCharts(): void {
    const yAxes = this.series.map((series, index) => {
      const max = this.getSeriesMax(series.data);
      const scale = this.getScale(max);
      return {
        seriesName: series.name,
        opposite: index > 0,
        title: {
          style: {
            color: series.color
          }
        },
        labels: {
          formatter: (value: number) => {
            const scaled = value / scale.divisor;

            return Number.isInteger(scaled)
              ? `${scaled}${scale.suffix}`
              : `${scaled.toFixed(1)}${scale.suffix}`;
          }
        },
        axisBorder: {
          show: true,
          color: series.color
        },
        axisTicks: {
          show: true,
          color: series.color
        }
      };
    });
    this.chartOptions = {
      series: this.series.map(series => ({
        name: series.name,
        data: series.data
      })),
      colors: this.series.map(series => series.color),
      chart: {
        type: 'line',
        height: 340,
        toolbar: {
          show: false
        },
        zoom: {
          enabled: false
        }
      },
      stroke: {
        curve: 'smooth',
        width: 3
      },
      markers: {
        size: 4
      },
      dataLabels: {
        enabled: false
      },
      legend: {
        position: 'top'
      },
      grid: {
        borderColor: '#ECECEC'
      },
      xaxis: {
        categories: this.buildLabels(),
        tickPlacement: 'on',
        labels: {
          rotate: 0,
          formatter: (value) => {
            if (value === undefined) return '';
            if (this.period === 'LAST_12_MONTHS') {
              return value;
            }
            if (this.period === 'LAST_3_YEARS' || this.period === 'LAST_5_YEARS') {
              const [month, year] = value.split(' ');
              return month === 'Jan' ? year : '';
            }
            return value;
          }
        }
      },
      yaxis: yAxes,
      tooltip: {
        shared: true,
        x: {
          formatter: (_value, opts) => {
            const index = opts.dataPointIndex;

            return `${this.MONTHS[this.growth.months[index] - 1]} ${this.growth.years[index]}`;
          }
        },
        y: {
          formatter: (value: number) => {
            return value.toLocaleString();
          }
        }
      }
    };
  }

  private getSeriesMax(data: number[]): number {
    return Math.max(...data);
  }

  private getScale(max: number): { divisor: number; suffix: string } {
    if (max >= 1000000) {
      return {
        divisor: 1000000,
        suffix: 'M'
      };
    }
    if (max >= 10000) {
      return {
        divisor: 1000,
        suffix: 'K'
      };
    }
    if (max >= 1000) {
      return {
        divisor: 1000,
        suffix: 'K'
      };
    }
    if (max >= 100) {
      return {
        divisor: 100,
        suffix: 'H'
      };
    }
    return {
      divisor: 1,
      suffix: ''
    };
  }
}