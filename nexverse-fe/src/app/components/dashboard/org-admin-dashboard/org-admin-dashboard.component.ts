import { Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { GrowthChartComponent } from '../../../shared/components/growth-chart/growth-chart.component';
import { FileSizePipe } from '../../../shared/pipes/file-size.pipe';
import { OrgAdminDashboardModel } from '../../../core/models/org-admin-dashboard.model';
import { GrowthSeries } from '../../../core/models/growth-series.model';
import { DashboardService } from '../../../core/services/dashboard.service';
import { ApiResponse } from '../../../core/models/api-response.model';

@Component({
  selector: 'app-org-admin-dashboard',
  imports: [
    MATERIAL_IMPORTS,
    GrowthChartComponent,
    FileSizePipe,
    DecimalPipe
  ],
  templateUrl: './org-admin-dashboard.component.html',
  styleUrl: './org-admin-dashboard.component.scss'
})
export class OrgAdminDashboardComponent implements OnInit {
  loading = true;
  dashboardData!: OrgAdminDashboardModel;
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
    this.dashboardService.getOrgAdminDashboard(this.selectedPeriod).subscribe({
      next: (response: ApiResponse<OrgAdminDashboardModel>) => {
        this.dashboardData = response.data as OrgAdminDashboardModel;
        this.growthSeries = [
          {
            name: 'Employees',
            data: this.dashboardData.growth.employeeCounts,
            color: '#d97959'
          },
          {
            name: 'Courses',
            data: this.dashboardData.growth.courseCounts,
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