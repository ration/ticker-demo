import {Component} from '@angular/core';
import {News} from './news.model';
import {TickerService} from './ticker-service';
import {Subscription} from 'rxjs';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  public subscription: Subscription = null;
  newsList: Array<News> = [];

  constructor(private tickerService: TickerService) {
  }

  public block() {
    const sec = 10000;
    console.log('Blocking js for ' + (sec / 1000) + ' seconds');
    const end = Date.now() + sec;
    while (Date.now() < end) {
      const doSomethingHeavyInJavaScript = 1 + 2 + 3;
    }
    console.log('Blocking done');

  }

  public stop() {
    if (!this.subscription.closed) {
      this.subscription.unsubscribe();
    } else {
      this.subscribe();
    }
  }


  public subscribe() {
    this.subscription = this.tickerService.ticker().subscribe(data => {
      this.newsList.push(data);
      if (this.newsList.length > 3) {
        this.newsList.shift();
      }
 });
  }

  public connect() {
    if (this.subscription == null) {
      this.tickerService.initSocket();
      this.subscribe();
    }
  }



}
