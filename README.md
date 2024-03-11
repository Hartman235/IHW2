Эта программа Kotlin имитирует систему управления рестораном, где пользователи могут добавлять, удалять и изменять блюда в меню, размещать заказы и управлять заказами. Он предоставляет интерфейс командной строки для взаимодействия с различными функциями.

## Функции

- **waiter add food <nameOfFood>**: официанты могут добавлять в меню новые блюда.
- **waiter remove food <nameOfFood>**: официанты могут удалять существующие блюда из меню.
- **display menu**: показывает меню - стоимость блюд и время их приготовления.
- **waiter food <nameOfFood> set number <number>**: официанты могут установить количество блюд, возможных к заказу.
- **waiter food <nameOfFood> set price <price>**: официанты могут изменить цену блюд.
- **waiter food <nameOfFood> set duration <duration>**: официанты могут изменить время приготовления блюд.
- **user order food <nameOfFood>**: пользователи могут добавить еду в текущий заказ.
- **user remove food <nameOfFood>**: пользователи могут удалять удалить еду из текущего заказа.
- **user cancel order**: пользователи могут отменить текущий заказ.
- **user get order**: пользователи могут получить заказ и оплатить его (если заказ готов).

## Компоненты

Программа состоит из следующих основных компонентов:

1. **Food**: представляет собой продукт питания с такими свойствами, как название, цена, продолжительность и количество.
2. **Order**. Управляет позициями в заказе пользователя и предоставляет функции для обработки и отмены заказов.
3. **Client**: представляет клиента, который взаимодействует с сервером для размещения и получения заказов.
4. **Сервер**: управляет заказами, пунктами меню и отслеживанием доходов. Он обрабатывает запросы от клиентов и соответствующим образом обновляет систему.
5. **Menu Manage**: обрабатывает операции, связанные с меню ресторана, такие как добавление, удаление и изменение блюд.
6. **Order Processor**: обрабатывает запросы пользователей, связанные с управлением заказами, например добавление/удаление товаров и отмену заказов.
7. **Input Handler**: анализирует вводимые пользователем команды и делегирует их соответствующим компонентам для выполнения.
8. **App Runner**: основная точка входа программы, которая запускает цикл приложения и обрабатывает ввод пользователя.

## Использование

1. **Starting the Program**:
    - Запустите функцию `main()`, чтобы запустить систему управления рестораном.
    - Следуйте инструкциям командной строки для взаимодействия с различными функциями.

2. **Команды**:
    - Используйте различные команды, такие как «официант добавить еду», «официант удалить еду», «пользователь заказывает еду» и т. д., чтобы выполнять действия внутри системы.

3. **Выход из программы**:
    - Введите `exit`, чтобы корректно выйти из программы.

## Обработка ошибок

- Программа обрабатывает такие ошибки, как добавление повторяющихся продуктов питания, удаление несуществующих продуктов, предоставление неполной информации и т. д., и выдает соответствующие сообщения об ошибках.
- Исключения, такие как IllegalArgumentException и RuntimeException, выдаются для обозначения исключительных условий и обеспечения надежности.

## Зависимости

- Программа не имеет внешних зависимостей и может запускаться с использованием стандартной библиотеки Kotlin.
