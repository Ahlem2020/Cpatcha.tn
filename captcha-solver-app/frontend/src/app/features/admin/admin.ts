import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.html',
  styleUrls: ['./admin.css'],
  standalone: true
})
export class AdminComponent implements OnInit {
  users: any;
  subscriptions: any;

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    this.http.get('http://localhost:8080/api/admin/users').subscribe(data => {
      this.users = data;
    });

    this.http.get('http://localhost:8080/api/admin/subscriptions').subscribe(data => {
      this.subscriptions = data;
    });
  }
}
