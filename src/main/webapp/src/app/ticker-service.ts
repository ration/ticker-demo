import {Injectable} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {JsonSerializers, RSocketClient, MAX_STREAM_ID} from 'rsocket-core';
import {News} from './news.model';
import {Observable, Subject} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class TickerService {

  private host = 'localhost';
  private port = 9988;
  private subject = new Subject<News>();
  private previous: News = null;

  constructor() {
  }

  public ticker(): Observable<News> {
    return this.subject.asObservable();
  }

  public initSocket() {
    const client = new RSocketClient({
      // send/receive objects instead of strings/buffers
      serializers: JsonSerializers,
      setup: {
        // ms btw sending keepalive to server
        keepAlive: 60000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        // format of `metadata`
        metadataMimeType: 'application/json',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://' + this.host + ':' + this.port,
        wsCreator: url => {
          return new WebSocket(url);
        }
      })
    });
    const self = this;
    client.connect().subscribe({
      onComplete: socket => {
        socket.requestStream({data: '', metadata: ''}).subscribe({
          onComplete() {
            console.log('onComplete()');
          },
          onError(error) {
            console.log('onError(%s)', error.message);
          },
          onNext(payload) {
            self.subject.next(payload.data);
            if (self.previous != null && (self.previous.id + 1) !== payload.data.id) {
              console.log('Missed ' + payload.data.id + " previous " + self.previous.id);
            }

            self.previous = payload.data;
          },
          onSubscribe(_subscription) {
            console.log('on subscribe');
            _subscription.request(MAX_STREAM_ID);
          }
        });
      },
      onError: error => console.error(error),
      onSubscribe: cancel => {/* call cancel() to abort */
      },
      onNext: data => console.log(data)
    });


  }

}
