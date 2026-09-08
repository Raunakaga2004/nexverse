import { Component } from '@angular/core';
import { ApiResponse } from '../../../core/models/api-response.model';
import { DashboardService } from '../../../core/services/dashboard.service';
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { EmployeeDashboardModel, EmployeeDashboardRecentCourses } from '../../../core/models/employee-dashboard.model';
import { DatePipe } from '@angular/common';
import { MatChipsModule } from "@angular/material/chips";
import { environment } from '../../../../environments/environment.development';
import { DurationPipe } from '../../../shared/pipes/duration.pipe';
import { RouterLink } from '@angular/router';
import { NoDataComponent } from "../../../shared/components/no-data/no-data.component";

@Component({
  selector: 'app-employee-dashboard',
  imports: [MATERIAL_IMPORTS, DatePipe, MatChipsModule, DurationPipe, RouterLink, NoDataComponent],
  templateUrl: './employee-dashboard.component.html',
  styleUrl: './employee-dashboard.component.scss'
})
export class EmployeeDashboardComponent {
  loading = true;
  dashboardData!: EmployeeDashboardModel;
  selectedPeriod = 'LAST_12_MONTHS';
  recentInProgressCourses: EmployeeDashboardRecentCourses[] = [];
  apiUrl = environment.apiUrl

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
    this.dashboardService.getEmployeeDashboard(this.selectedPeriod).subscribe({
      next: (response: ApiResponse<EmployeeDashboardModel>) => {
        this.dashboardData = response.data as EmployeeDashboardModel;
        this.recentInProgressCourses = this.dashboardData.recentInProgressCourses;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}