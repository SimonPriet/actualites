import { moment } from 'entcore';

export class Utils {
    static getDateAsMoment (date) {
        var momentDate;
        if (moment.isMoment(date)) {
            momentDate = date;
        }
        else if (typeof date !== 'string' && date.$date) {
            momentDate = moment(date.$date);
        } else if (typeof date === 'number'){
            momentDate = moment.unix(date);
        } else {
            momentDate = moment(date);
        }
        return momentDate;
    }

    static getExploitableDate (date:string):Date{
        try{
        const dateHash:string[] = date.split("/");
        return moment(new Date(
            parseInt(dateHash[2]),
            parseInt((parseInt(dateHash[1]) -1).toString(10)),
            parseInt(dateHash[0]),
            0,
            0,
            0,
        ));
        } catch (error) {
            throw error;
        }
    }
    static isDateFormateCompatible(date:any):Boolean{
        return (typeof date === "string" && date.length === 10 && date.includes("/"));
    }
}