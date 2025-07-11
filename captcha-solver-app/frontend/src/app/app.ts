import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router'; // Added RouterLink for navigation items
import { CommonModule } from '@angular/common'; // Added CommonModule for *ngIf
import { TokenStorageService } from './auth/token-storage.service';
import { AuthService } from './auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true, // Explicitly declare as standalone
  imports: [RouterOutlet, RouterLink, CommonModule], // Added RouterLink and CommonModule
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit { // Renamed to AppComponent and implements OnInit
  protected readonly title = signal('CaptchaSolver'); // Updated title

  isLoggedIn = false;
  username?: string;
  // roles: string[] = []; // If you need to display roles

  constructor(
    private tokenStorageService: TokenStorageService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.tokenStorageService.isLoggedIn();

    if (this.isLoggedIn) {
      const user = this.tokenStorageService.getUser();
      this.username = user.username;
      // this.roles = user.roles; // Uncomment if roles are needed
    }
  }

  logout(): void {
    this.authService.logout();
    this.isLoggedIn = false;
    // window.location.reload(); // Or navigate to login
    this.router.navigate(['/login']).then(() => {
      window.location.reload(); // Ensure page reloads to clear everything if needed
    });
  }
}
