import { Component, OnInit } from '@angular/core';
import { NbAccessChecker } from '@nebular/security';
import { NbSidebarService, NbMenuService, NbMenuItem } from '@nebular/theme';
import { admin_menu } from './pages-menu';

@Component({
  selector: 'app-pages',
  templateUrl: './pages.component.html',
  styleUrls: ['./pages.component.scss']
})
export class PagesComponent implements OnInit {

  constructor( private sidebarService: NbSidebarService,
               private menuService: NbMenuService,
               private accessChecker: NbAccessChecker) {

  }

  menu: NbMenuItem[] = admin_menu;
  
  toggleSidebar(): boolean {
    this.sidebarService.toggle(true, 'menu-sidebar');
    this.isExpanded();
    return false;
  }

  isExpanded(): boolean {
    this.sidebarService.onExpand().subscribe(function(e) {
      console.log(e);
    });
    return true;
  }

  gotoHome() {
      this.menuService.navigateHome();
  }


  authMenuItems() {
    this.menu.forEach(item => {
      this.authMenuItem(item);
    });
  }

  authMenuItem(menuItem: NbMenuItem) {
    menuItem.hidden = true;
    if (menuItem.data && menuItem.data['permission'] && menuItem.data['resource']) {

      menuItem.data['resource'].forEach(element => {   
        this.accessChecker.isGranted(menuItem.data['permission'], element).subscribe(granted => {
          if(granted)
            menuItem.hidden = false;
        });
      });
    } else {
      menuItem.hidden = true;
    }
    if (!menuItem.hidden && menuItem.children != null) {
      menuItem.children.forEach(item => {
        if (item.data && item.data['permission'] && item.data['resource']) {
          this.accessChecker.isGranted(item.data['permission'], item.data['resource']).subscribe(granted => {
            if(granted)
            item.hidden = false;
          });
        } else {
          // if child item do not config any `data.permission` and `data.resource` just inherit parent item's config
          item.hidden = menuItem.hidden;
        }
      });
    }
  }

  ngOnInit() {
    this.authMenuItems();
  }
}
