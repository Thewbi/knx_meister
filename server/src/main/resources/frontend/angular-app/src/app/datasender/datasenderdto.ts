export class DataSenderDto {

    constructor(
        public id: number,
        public dataGeneratorType: string,
        public upperBound: number,
        public lowerBound: number,
        public constant: number) { }
}