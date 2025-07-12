import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TokenStorageService } from '../features/auth/token-storage.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  standalone: true,
  imports: [RouterModule, CommonModule]
})
export class NavbarComponent {
  isLoggedIn = false;
  username?: string;
  isAdmin = false;

  constructor(private tokenStorageService: TokenStorageService, private router: Router) { }

  ngOnInit(): void {
    this.isLoggedIn = !!this.tokenStorageService.getToken();

    if (this.isLoggedIn) {
      const user = this.tokenStorageService.getUser();
      this.username = user.username;
      this.isAdmin = user.roles.includes('ROLE_ADMIN');
    }
  }

  logout(): void {
    this.tokenStorageService.signOut();
    this.router.navigate(['/login']);
  }

  title() {
    return 'Captcha Solver';
  }
}
