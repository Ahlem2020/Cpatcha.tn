import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { TokenStorageService } from './token-storage.service';

const AUTH_API = 'http://localhost:8080/api/auth/'; // Adjust if your backend runs elsewhere

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient, private tokenStorageService: TokenStorageService) { }

  login(credentials: any): Observable<any> {
    return this.http.post(AUTH_API + 'signin', {
      username: credentials.username,
      password: credentials.password
    }, httpOptions).pipe(
      tap(data => { // Use tap for side-effects like storing token
        if (data && data.token) { // Assuming response structure is { token: '...', ...otherUserDetails }
          this.tokenStorageService.saveToken(data.token);
          // The JwtResponse includes id, username, email, roles.
          // These are directly compatible with what saveUser might expect.
          this.tokenStorageService.saveUser(data);
        }
      })
    );
  }

  logout(): void {
    this.tokenStorageService.signOut();
    // Optionally, could also make a backend call to invalidate token if server supports it
  }

  register(user: any): Observable<any> {
    return this.http.post(AUTH_API + 'signup', {
      username: user.username,
      email: user.email,
      password: user.password
    }, httpOptions);
  }

  isLoggedIn(): boolean {
    return this.tokenStorageService.isLoggedIn();
  }

  getCurrentUser(): any {
    return this.tokenStorageService.getUser();
  }
}
