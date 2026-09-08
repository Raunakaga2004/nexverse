import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from "@angular/router";
import { MATERIAL_IMPORTS } from '../../../core/material/matrerial';
import { SidebarItem } from '../../../core/models/sidebar-item.model';
import { SidebarService } from '../../../core/services/sidebar.service';
import { CurrentUserService } from '../../../core/services/current-user.service';

@Component({
  selector: 'app-dashboard-sidebar',
  imports: [RouterLink, RouterLinkActive, MATERIAL_IMPORTS],
  templateUrl: './dashboard-sidebar.component.html',
  styleUrl: './dashboard-sidebar.component.scss'
})
export class DashboardSidebarComponent implements OnInit{
  sidebarItems : SidebarItem[] = [];
  managerPersonalSidebarItems : SidebarItem[] | undefined;
  currentRole : string | undefined;
  constructor(
    private sidebarService : SidebarService,
    private currentUserService : CurrentUserService
  ){}
  ngOnInit(): void {
    this.currentRole = this.currentUserService.getCurrentUserRole();
    this.sidebarItems = this.sidebarService.getSidebarItems();
    this.managerPersonalSidebarItems = this.sidebarService.getPersonalManagerItems();
  }
}