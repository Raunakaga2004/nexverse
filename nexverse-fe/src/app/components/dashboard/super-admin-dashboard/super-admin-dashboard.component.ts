import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { FileSizePipe } from '../../../shared/pipes/file-size.pipe';
import { RecentOrganizations, SuperAdminDashboardModel } from '../../../core/models/super-admin-dashboard.model';
import { GrowthSeries } from '../../../core/models/growth-series.model';
import { DashboardService } from '../../../core/services/dashboard.service';
import { ApiResponse } from '../../../core/models/api-response.model';
import { GrowthChartComponent } from '../../../shared/components/growth-chart/growth-chart.component';

@Component({
  selector: 'app-super-admin-dashboard',
  imports: [MATERIAL_IMPORTS, FileSizePipe, GrowthChartComponent],
  templateUrl: './super-admin-dashboard.component.html',
  styleUrl: './super-admin-dashboard.component.scss'
})
export class SuperAdminDashboardComponent implements OnInit {
  loading = true;
  dashboardData!: SuperAdminDashboardModel;
  selectedPeriod = "LAST_12_MONTHS";
  displayedColumns: string[] = [
    'organization',
    'admin',
    'status',
    'createdAt'
  ]
  growthSeries: GrowthSeries[] = [];


  dataSource = new MatTableDataSource<RecentOrganizations>();
  constructor(private dashboardService: DashboardService) { }
  ngOnInit(): void {
    this.loadDashboard();
  }
  onPeriodChange(period: string) {
    this.selectedPeriod = period;
    this.loadDashboard();
  }
  loadDashboard() {
    this.dashboardService.getSuperAdminDashboard(this.selectedPeriod).subscribe({
      next: (response: ApiResponse<SuperAdminDashboardModel>) => {
        this.dashboardData = response.data as SuperAdminDashboardModel;
        this.growthSeries = [{
          name: 'Number of Organizations',
          data: this.dashboardData.growth.organizationCounts,
          color: '#d97959'
        },
        {
          name: 'Number of Users',
          data: this.dashboardData.growth.userCounts,
          color: '#D6B88C'
        }]
        this.dataSource = new MatTableDataSource<RecentOrganizations>(this.dashboardData.recentOrganizations)
        this.loading = false;
      }
    });
  }
}