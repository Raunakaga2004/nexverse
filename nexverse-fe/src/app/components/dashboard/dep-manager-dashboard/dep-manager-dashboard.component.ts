import { Component, OnInit } from '@angular/core';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { GrowthChartComponent } from '../../../shared/components/growth-chart/growth-chart.component';
import { DepManagerDashboardModel } from '../../../core/models/dep-manager-dashboard.model';
import { DashboardService } from '../../../core/services/dashboard.service';
import { ApiResponse } from '../../../core/models/api-response.model';
import { GrowthSeries } from '../../../core/models/growth-series.model';

@Component({
  selector: 'app-dep-manager-dashboard',
  imports: [
    MATERIAL_IMPORTS,
    GrowthChartComponent
  ],
  templateUrl: './dep-manager-dashboard.component.html',
  styleUrl: './dep-manager-dashboard.component.scss'
})
export class DepManagerDashboardComponent implements OnInit {
  loading = true;
  dashboardData!: DepManagerDashboardModel;
  selectedPeriod = 'LAST_12_MONTHS';
  growthSeries: GrowthSeries[] = [];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.loadDashboard();
  }

  onPeriodChange(period: string): void {
    this.selectedPeriod = period;
    this.loadDashboard();
  }

  private loadDashboard(): void {
    this.loading = true;
    this.dashboardService.getDepManagerDashboard(this.selectedPeriod).subscribe({
      next: (response: ApiResponse<DepManagerDashboardModel>) => {
        this.dashboardData = response.data as DepManagerDashboardModel;
        this.growthSeries = [
          {
            name: 'Completed Courses',
            data: this.dashboardData.growth.completedCourses,
            color: '#d97959'
          },
          {
            name: 'Assigned Courses',
            data: this.dashboardData.growth.assignedCourses,
            color: '#D6B88C'
          }
        ];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}