import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { DataSenderDto } from './datasenderdto';

@Injectable({
  providedIn: 'root'
})
export class DatasenderService {

  constructor(private http: HttpClient) { }

  createDataSender(physialAddress: string, groupAddress: string, dataSender: DataSenderDto) {

    // the format of a group address is a/b/c but to inserte a group address into a 
    // URL, the slash is replaced by dots.
    const tempGroupAddress = groupAddress.replace(/\//g, '.');

    const url = 'http://localhost:8189/knxmeister/api/datagenerator/add/' + physialAddress + '/' + tempGroupAddress;

    console.log(url);

    // const httpHeaders = new HttpHeaders({
    //   'Content-Type': 'application/json'
    // });

    const httpHeaders = new HttpHeaders({
      'Content-Type': 'application/json',
      responseType: 'text' as 'json'
    });

    const body = JSON.stringify(dataSender);

    // post with body
    return this.http.post(url, body, { headers: httpHeaders });
  }
}
