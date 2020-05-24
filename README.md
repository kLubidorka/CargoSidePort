## Постановка задачи  
*Условия исходной задачи обобщены на случай произвольного времени 
прохождения канала, числа кораблей в нем, числа ресурсов и скорости их погрузки.*  
  
Существует генератор транспортных кораблей. В произвольный момент времени генератор
может мгновенно создать корабль с заданной вместимостью способный перевозить один из M ресурсов. 
Также существует M причалов, каждый из которых может погружать ровно один тип ресурсов на корабль. Количество ресурсов 
на причале не ограничено. Для каждого ресурса 
найдется причал, который может погружать его на корабль. В каждый момент времени причал может обслуживать только один 
корабль, причем скорость погрузки ресурса i равна v_{i} ед. в секунду. После полной погрузки корабль освобождает место 
на причале и уходит в море.  
Единственный путь между генератором и причалами проходит через узкий канал, способный пропускать не более N кораблей 
одновременно. Прохождение корабля через канал занимает время T, не зависящее от количества кораблей в канале. 
От генератора до канала и от канала до причала корабль перемещается за пренебрежимо малое время.  
Требуется реализовать многопоточное приложение, эмулирующее работу описанной системы, в котором каждый 
корабль - это поток исполнения.

* Работа причалов и генератора независимы.
* Потоки должны использовать время исполнения и lock-и эффективно.
* Общий ресурс должен быть потокобезопасным
  
## Описание решения  
  
Решение будем строить из следующих предположений. Во-первых, будем считать, что основная работа -- это загрузка кораблей 
ресурсами, и она должна происходить максимально эффективно, то есть нужно минимизировать суммарное время простоя причалов (*первое условие*).
Во-вторых, одинаковое время простоя конкретного причала может быть достигнуто при различном относительном порядке 
прохода кораблей к этому причалу. Из всех таких относительных порядков будем выбирать тот, при котором корабли, предназначенные 
для перевозки фиксированного ресурса X, проходят в том же порядке, в котором они появились (*второе условие*). Если выполнены эти два условия, 
то исключается thread starving, так как если корабль проходит к причалу за необоснованно большое время, то и все корабли, 
перевозящие тот же ресурс и появившиеся после него, тоже проходят маршрут долго, значит, причал, загружающий данный ресурс,
необоснованно долгое время бездействует, что противоречит первому условию. Для того чтобы балансировать нагрузку на 
причалы, нужно, чтобы порядок, в котором корабли попадают в канал, был оптимальным в смысле первого условия. Для этого 
введем функцию приоритета ресурса. P(k) = -\sum\limits_{i}c_{i} \cdot  v_{k}, то есть мы умножаем сумму грузоподъемностей 
кораблей для данного ресурса, которые уже прошли в канал, но еще не загрузились на причале, на время погрузки единицы данного товара 
и усножаем на минус один. Таким образом, приоритет тем выше, чем скорее освободится причал. В канал будем пускать корабль, 
ресурс которого имеет наибольший приоритет из доступных на данный момент.  
  
## Реализация  
  
Реализовыввать решение будем при помощи семафоров с параметром fair=true, чтобы обеспечить FIFO (второе условие). 
Появившиеся потоки разделяем на M групп (в каждой группе корабли, перевозящие один и тот же товар). Для каждой группы 
определим свой семафор с permits=1. Помимо этого определим семафор с permits=N, соответствующий каналу. Еще нам потребуется 
M семафоров, по одному на причал. Кроме того будем хранить M атомарных переменных типа boolean, по одной на группу. 
Каждая такая переменная определяет, может ли поток из группы пытаться захватить семафор канала. В начальный момент времени 
все такие переменные имеют значение true.  
После того как поток появляется, он блокируется до захвата семафора своей группы. После захвата он пытается при помощи CAS поменять 
значение boolean переменной своей группы с true на false, и, поменяв, отпускает семафор группы, затем блокируется до захвата семафора канала. 
Захватив семафор канала, поток вызывает synchronized функцию, которая пересчитывает приоритеты 
и выдает новые разрешения на попытку захвата семафора канала, после чего засыпает на время T, что соответствует 
прохождению кораблем канала. Проснувшись, поток отпускает семафор канала и блокируется до захвата семафора своего причала. 
Захватив семафор причала, поток засывает, что соответствует загрузке корабля ресурсами. После этого поток отпускает 
семафор причала и запускает функцию пересчета.  
В данном случае использование synchronized функции не портит параллельность, так как каждый поток вызывает ее ровно два раза 
и время ее выполнения мало по сравнению с полезной работой (загрузка на причале и прохождение канала).
